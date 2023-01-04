package ru.ivk1800.vcs.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsFile
import ru.ivk1800.vcs.git.parser.GitLogParser
import ru.ivk1800.vcs.git.parser.VcsDiffParser
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.exists

class GitVcs : Vcs {
    private val parser = VcsParser()
    private val diffParser = VcsDiffParser()
    private val separatorBuilder = SeparatorBuilder()
    private val gitLogParser = GitLogParser(separatorBuilder)

    override suspend fun isRepository(directory: File): Boolean =
        withContext(Dispatchers.IO) {
            if (!directory.exists()) {
                false
            } else {
                val isRepo = createProcess(directory, "git rev-parse --git-dir").exitValue() == 0
                isRepo && Path(directory.path, ".git").exists()
            }
        }

    override suspend fun getCommits(
        directory: File,
        branchName: String,
        limit: Int,
        offset: Int,
    ): List<VcsCommit> = withContext(Dispatchers.IO) {
        val pretty = buildString {
            append(separatorBuilder.startRecordSeparator())
            append("%n")
            addOption(GitLogOption.Hash)
            addOption(GitLogOption.Parents)
            addOption(GitLogOption.AbbreviatedHash)
            addOption(GitLogOption.RawBody)
            addOption(GitLogOption.AuthorName)
            addOption(GitLogOption.AuthorEmail)
            addOption(GitLogOption.AuthorDate)
            addOption(GitLogOption.CommiterName)
            addOption(GitLogOption.CommiterEmail)
            addOption(GitLogOption.CommiterDate)
            addOption(GitLogOption.RefName)
            append(separatorBuilder.endRecordSeparator())
        }

        val process = createProcess(
            directory,
            "git log --pretty=format:$pretty -n $limit, --skip $offset $branchName",
        )

        val result = process.inputStream.reader().readText()
        val error = process.errorStream.reader().readText()
        gitLogParser.parseLog(result)
    }

    override suspend fun getCommit(directory: File, hash: String): VcsCommit? {
        val process = createProcess(
            directory,
            "git log -1 --format={$FIELDS} $hash",
        )
        val result = process.inputStream.reader().readText()
        val error = process.errorStream.reader().readText()
        println(error)
        println(result)

        return parser.parseCommit(result)
    }

    override suspend fun getCommitFiles(directory: File, commitHash: String): List<VcsFile> {
        val process = createProcess(
            directory,
            "git show --name-only $commitHash -n 1 --oneline",
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

    private inline fun <T> runProcess(
        process: Process,
        onResult: (result: String) -> T,
        onError: (error: String) -> Throwable,
    ): T = if (process.exitValue() != 0) {
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

    private fun StringBuilder.addOption(option: GitLogOption) {
        append("${separatorBuilder.buildStartForOption(option)}%n")
        append("%${option.value}%n")
        append("${separatorBuilder.buildEndForOption(option)}%n")
    }

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
