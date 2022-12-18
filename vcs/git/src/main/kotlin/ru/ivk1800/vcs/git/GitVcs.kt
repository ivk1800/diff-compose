package ru.ivk1800.vcs.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.api.VcsFile
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.io.path.exists

class GitVcs : Vcs {
    private val parser = VcsParser()

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
        val process = createProcess(
            directory,
            "git reflog --pretty={$FIELDS}, -n $limit, --skip $offset $branchName",
        )

        val result = process.inputStream.reader().readText()
        val error = process.errorStream.reader().readText()
        println(error)
        parser.parseCommits("[$result]")
    }

    override suspend fun getCommitFiles(directory: File, commitHash: String): List<VcsFile> {
        val process = createProcess(
            directory,
            "git show --name-only $commitHash -n 1 --oneline",
        )

        val result = process.inputStream.reader().readText()
        return parser.parseFiles(result)
    }

    private fun createProcess(directory: File, command: String): Process =
        ProcessBuilder(*command.split(" ").toTypedArray())
            .directory(directory)
            .start()
            .apply { waitFor(10, TimeUnit.SECONDS) }

    private companion object {
        val FIELDS = listOf(
            """"hash":"%H"""",
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
