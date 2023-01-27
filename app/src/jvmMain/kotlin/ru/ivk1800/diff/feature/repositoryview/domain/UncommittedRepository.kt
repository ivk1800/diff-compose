package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.command.AddFilesToStagedCommand
import ru.ivk1800.vcs.api.command.RemoveFilesFromStagedCommand
import java.io.File

class UncommittedRepository(
    private val repoDirectory: File,
    private val vcs: Vcs,
) {
    suspend fun getUntrackedFiles(): List<String> =
        vcs.getUntrackedFilesCommand(repoDirectory.toPath()).run()

    suspend fun removeAllFromStaged() = vcs.getRemoveAllFromStagedCommand(repoDirectory.toPath()).run()

    suspend fun addAllToStaged() = vcs.getAddAllToStagedCommand(repoDirectory.toPath()).run()

    suspend fun removeFilesFromStaged(filePaths: List<String>) {
        check(filePaths.isNotEmpty()) {
            "It makes no sense to remove nothing from the stage"
        }
        vcs.getRemoveFilesFromStagedCommand(
            repoDirectory.toPath(),
            RemoveFilesFromStagedCommand.Options(filePaths),
        )
            .run()
    }

    suspend fun addFilesToStaged(filePaths: List<String>) {
        check(filePaths.isNotEmpty()) {
            "It makes no sense to add nothing to the stage"
        }
        vcs.getAddFilesToStagedCommand(repoDirectory.toPath(), AddFilesToStagedCommand.Options(filePaths)).run()
    }
}
