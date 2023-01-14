package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import java.io.File

class UncommittedRepository(
    private val repoDirectory: File,
    private val vcs: Vcs,
) {
    suspend fun getUntrackedFiles(): List<String> =
        vcs.getUntrackedFilesCommand(repoDirectory.toPath()).run()

    suspend fun removeAllFromStaged() = vcs.removeAllFromStaged(repoDirectory)

    suspend fun addAllToStaged() = vcs.addAllToStaged(repoDirectory)

    suspend fun removeFilesFromStaged(filePaths: List<String>) {
        check(filePaths.isNotEmpty())
        vcs.removeFilesFromStaged(repoDirectory, filePaths)
    }

    suspend fun addFilesToStaged(filePaths: List<String>) {
        check(filePaths.isNotEmpty()) {
            "It makes no sense to add anything to the stage"
        }
        vcs.addFilesToStaged(repoDirectory, filePaths)
    }
}
