package ru.ivk1800.vcs.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.diff.logging.Logger
import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.AddAllToStagedCommand
import ru.ivk1800.vcs.api.command.AddFilesToStagedCommand
import ru.ivk1800.vcs.api.command.DiffCommand
import ru.ivk1800.vcs.api.command.DiscardCommand
import ru.ivk1800.vcs.api.command.GetCommitCommand
import ru.ivk1800.vcs.api.command.GetCommitFilesCommand
import ru.ivk1800.vcs.api.command.GetCommitsCommand
import ru.ivk1800.vcs.api.command.GetStashListCommand
import ru.ivk1800.vcs.api.command.GetUntrackedFilesCommand
import ru.ivk1800.vcs.api.command.HashObjectCommand
import ru.ivk1800.vcs.api.command.RemoveAllFromStagedCommand
import ru.ivk1800.vcs.api.command.RemoveFilesFromStagedCommand
import ru.ivk1800.vcs.api.command.ShowCommand
import ru.ivk1800.vcs.api.command.StatusCommand
import ru.ivk1800.vcs.api.command.UpdateIndexCommand
import ru.ivk1800.vcs.git.command.AddAllToStagedCommandImpl
import ru.ivk1800.vcs.git.command.AddFilesToStagedCommandImpl
import ru.ivk1800.vcs.git.command.DiffCommandImpl
import ru.ivk1800.vcs.git.command.DiscardCommandImpl
import ru.ivk1800.vcs.git.command.GetCommitCommandImpl
import ru.ivk1800.vcs.git.command.GetCommitFilesCommandImpl
import ru.ivk1800.vcs.git.command.GetCommitsCommandImpl
import ru.ivk1800.vcs.git.command.GetStashListCommandImpl
import ru.ivk1800.vcs.git.command.GetUntrackedFilesCommandImpl
import ru.ivk1800.vcs.git.command.HashObjectCommandImpl
import ru.ivk1800.vcs.git.command.RemoveAllFromStagedCommandImpl
import ru.ivk1800.vcs.git.command.RemoveFilesFromStagedCommandImpl
import ru.ivk1800.vcs.git.command.ShowCommandImpl
import ru.ivk1800.vcs.git.command.StatusCommandImpl
import ru.ivk1800.vcs.git.command.UpdateIndexCommandImpl
import ru.ivk1800.vcs.git.parser.GitLogParser
import ru.ivk1800.vcs.git.parser.GitShowParser
import ru.ivk1800.vcs.git.parser.GitStashListParser
import ru.ivk1800.vcs.git.parser.GitStatusParser
import ru.ivk1800.vcs.git.parser.VcsDiffParser
import java.io.File
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.exists

class GitVcs(
    private val logger: Logger,
) : Vcs {
    private val showParser = GitShowParser()
    private val gitStatusParser = GitStatusParser()
    private val diffParser = VcsDiffParser()
    private val separatorBuilder = SeparatorBuilder()
    private val stashListParser = GitStashListParser()
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

    override suspend fun getCommitCommand(directory: Path, options: GetCommitCommand.Options): GetCommitCommand =
        GetCommitCommandImpl(gitLogParser, separatorBuilder, directory, commandContext, options)

    override suspend fun getCommitFilesCommand(
        directory: Path,
        options: GetCommitFilesCommand.Options,
    ): GetCommitFilesCommand = GetCommitFilesCommandImpl(showParser, directory, options, commandContext)

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

    override suspend fun getRemoveAllFromStagedCommand(directory: Path): RemoveAllFromStagedCommand =
        RemoveAllFromStagedCommandImpl(directory, commandContext)

    override suspend fun getAddAllToStagedCommand(directory: Path): AddAllToStagedCommand =
        AddAllToStagedCommandImpl(directory, commandContext)

    override suspend fun getRemoveFilesFromStagedCommand(
        directory: Path,
        options: RemoveFilesFromStagedCommand.Options,
    ): RemoveFilesFromStagedCommand =
        RemoveFilesFromStagedCommandImpl(directory, options, commandContext)

    override suspend fun getAddFilesToStagedCommand(
        directory: Path,
        options: AddFilesToStagedCommand.Options,
    ): AddFilesToStagedCommand = AddFilesToStagedCommandImpl(directory, options, commandContext)

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

    override suspend fun getStashListCommand(directory: Path): GetStashListCommand =
        GetStashListCommandImpl(stashListParser, directory, commandContext)

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
}
