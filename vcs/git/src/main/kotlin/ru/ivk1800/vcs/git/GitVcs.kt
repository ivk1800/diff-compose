package ru.ivk1800.vcs.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsFile
import ru.ivk1800.vcs.api.command.DiffCommand
import ru.ivk1800.vcs.api.command.DiscardCommand
import ru.ivk1800.vcs.api.command.GetCommitsCommand
import ru.ivk1800.vcs.api.command.GetUntrackedFilesCommand
import ru.ivk1800.vcs.api.command.HashObjectCommand
import ru.ivk1800.vcs.api.command.ShowCommand
import ru.ivk1800.vcs.api.command.StatusCommand
import ru.ivk1800.vcs.api.command.UpdateIndexCommand
import ru.ivk1800.vcs.git.command.DiffCommandImpl
import ru.ivk1800.vcs.git.command.DiscardCommandImpl
import ru.ivk1800.vcs.git.command.GetCommitsCommandImpl
import ru.ivk1800.vcs.git.command.GetUntrackedFilesCommandImpl
import ru.ivk1800.vcs.git.command.HashObjectCommandImpl
import ru.ivk1800.vcs.git.command.ShowCommandImpl
import ru.ivk1800.vcs.git.command.StatusCommandImpl
import ru.ivk1800.vcs.git.command.UpdateIndexCommandImpl
import ru.ivk1800.vcs.git.parser.GitLogParser
import ru.ivk1800.vcs.git.parser.GitStatusParser
import ru.ivk1800.vcs.git.parser.VcsDiffParser
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.exists

class GitVcs : Vcs {
    private val parser = VcsParser()
    private val gitStatusParser = GitStatusParser()
    private val diffParser = VcsDiffParser()
    private val separatorBuilder = SeparatorBuilder()
    private val gitLogParser = GitLogParser(separatorBuilder)
    private val commandContext = Dispatchers.IO

    override suspend fun isRepository(directory: File): Boolean =
        withContext(Dispatchers.IO) {
            if (!directory.exists()) {
                false
            } else {
                val isRepo = createProcess(directory, "git rev-parse --git-dir").exitValue() == 0
                isRepo && Path(directory.path, ".git").exists()
            }
        }

    override suspend fun getCommitsCommand(directory: Path, options: GetCommitsCommand.Options): GetCommitsCommand =
        GetCommitsCommandImpl(gitLogParser, separatorBuilder, directory, commandContext, options)

    override suspend fun getCommit(directory: File, hash: String): VcsCommit? {
        val process = createProcess(
            directory,
            "git log -1 --format={$FIELDS} $hash",
        )
        val result = process.inputStream.reader().readText()
        val error = process.errorStream.reader().readText()

        return parser.parseCommit(result)
    }

    override suspend fun getCommitFiles(directory: File, commitHash: String): List<VcsFile> {
        val process = createProcess(
            directory,
            "git show $commitHash --name-status --oneline",
        )

        val result = process.inputStream.reader().readText()
        return parser.parseFiles(result)
    }

    override suspend fun getDiff(
        directory: File,
        oldCommitHash: String,
        newCommitHash: String,
        filePath: String,
    ): VcsDiff {
        val command = "git diff $oldCommitHash..$newCommitHash $filePath"
        val process = createProcess(
            directory,
            command,
        )

        return runProcess(process,
            onError = { error ->
                VcsException.ProcessException(error)
            }, onResult = { result ->
                diffParser.parseSingle(result)
            }
        )
    }

    override suspend fun getDiff(
        directory: File,
        oldBlobId: String,
        newBlobId: String,
    ): VcsDiff {
        val command = "git diff $oldBlobId..$newBlobId"
        val process = createProcess(
            directory,
            command,
        )

        return runProcess(process,
            onError = { error ->
                VcsException.ProcessException(error)
            }, onResult = { result ->
                diffParser.parseSingle(result)
            }
        )
    }

    override suspend fun getDiffCommand(directory: Path, options: DiffCommand.Options): DiffCommand =
        DiffCommandImpl(diffParser, directory, options)

    override suspend fun getUnStagedDiff(directory: File): List<VcsDiff> =
        runProcess(createProcess(directory, "git diff"),
            onError = { error ->
                VcsException.ProcessException(error)
            }, onResult = { result ->
                diffParser.parseMultiple(result)
            }
        )

    override suspend fun getStagedDiff(directory: File): List<VcsDiff> =
        runProcess(createProcess(directory, "git diff --cached"),
            onError = { error ->
                VcsException.ProcessException(error)
            }, onResult = { result ->
                diffParser.parseMultiple(result)
            }
        )

    override suspend fun removeAllFromStaged(directory: File) =
        runProcess(
            createProcess(directory, "git reset HEAD"),
            onError = { error ->
                VcsException.ProcessException(error)
            },
            onResult = { },
        )

    override suspend fun addAllToStaged(directory: File) =
        runProcess(
            createProcess(directory, "git add -A"),
            onError = { error ->
                VcsException.ProcessException(error)
            },
            onResult = { },
        )

    override suspend fun removeFilesFromStaged(directory: File, filePaths: List<String>) =
        runProcess(
            createProcess(directory, "git reset HEAD ${filePaths.joinToString(" ")}"),
            onError = { error ->
                VcsException.ProcessException(error)
            },
            onResult = { },
        )

    override suspend fun addFilesToStaged(directory: File, filePaths: List<String>) =
        runProcess(
            createProcess(directory, "git add ${filePaths.joinToString(" ")}"),
            onError = { error ->
                VcsException.ProcessException(error)
            },
            onResult = { },
        )

    override suspend fun getHashObjectCommand(directory: Path, options: HashObjectCommand.Options): String =
        HashObjectCommandImpl(directory, options).run()

    override suspend fun getShowCommand(directory: Path, options: ShowCommand.Options): ShowCommand =
        ShowCommandImpl(directory, options)

    override suspend fun getUpdateIndexCommand(
        directory: Path,
        options: UpdateIndexCommand.Options,
    ): UpdateIndexCommand =
        UpdateIndexCommandImpl(directory, options)

    override suspend fun getDiscardCommand(directory: Path, options: DiscardCommand.Options): DiscardCommand =
        DiscardCommandImpl(directory, options)

    override suspend fun getUntrackedFilesCommand(directory: Path): GetUntrackedFilesCommand =
        GetUntrackedFilesCommandImpl(directory)

    override suspend fun getStatusCommand(directory: Path): StatusCommand =
        StatusCommandImpl(directory, gitStatusParser)

    private inline fun <T> runProcess(
        process: Process,
        onResult: (result: String) -> T,
        onError: (error: String) -> Throwable,
    ): T = if (process.waitFor() != 0) {
        val error = process.errorStream.reader().readText()
        throw onError.invoke(error)
    } else {
        val result = process.inputStream.reader().readText()
//        if (result.isEmpty()) {
//            throw onError.invoke("result of process is empty")
//        } else {
        onResult.invoke(result)
//        }
    }

    private fun createProcess(directory: File, command: String): Process =
        ProcessBuilder(*command.split(" ").toTypedArray())
            .directory(directory)
            .start()
            .apply { waitFor(10, TimeUnit.SECONDS) }

    private companion object {
        val FIELDS = listOf(
            """"hash":"%H"""",
            """"parents":"%P"""",
            """"abbreviatedHash":"%h"""",
            """"authorName":"%an"""",
            """"authorEmail":"%ae"""",
            """"authorDate":"%at"""",
            """"commiterName":"%cn"""",
            """"commiterEmail":"%ce"""",
            """"commiterDate":"%ct"""",
            """"message":"%B"""",
        ).joinToString(",")
    }
}
