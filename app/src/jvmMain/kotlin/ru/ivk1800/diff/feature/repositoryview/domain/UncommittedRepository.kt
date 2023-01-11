package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import java.io.File

class UncommittedRepository(
    private val vcs: Vcs,
) {
    suspend fun getUntrackedFiles(directory: File): List<String> =
        vcs.getUntrackedFilesCommand(directory.toPath()).run()

    suspend fun removeAllFromStaged(directory: File) = vcs.removeAllFromStaged(directory)

    suspend fun addAllToStaged(directory: File) = vcs.addAllToStaged(directory)

    suspend fun removeFilesFromStaged(directory: File, filePaths: List<String>) {
        check(filePaths.isNotEmpty())
        vcs.removeFilesFromStaged(directory, filePaths)
    }

    suspend fun addFilesToStaged(directory: File, filePaths: List<String>) {
        check(filePaths.isNotEmpty()) {
            "It makes no sense to add anything to the stage"
        }
        vcs.addFilesToStaged(directory, filePaths)
    }
}
