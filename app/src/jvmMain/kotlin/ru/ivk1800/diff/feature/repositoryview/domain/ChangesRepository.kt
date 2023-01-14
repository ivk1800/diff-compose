package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.command.DiscardCommand
import ru.ivk1800.vcs.api.command.UpdateIndexCommand
import java.io.File

class ChangesRepository(
    private val repoDirectory: File,
    private val vcs: Vcs,
) {
    suspend fun updateIndex(fileName: String, content: String) =
        vcs.getUpdateIndexCommand(
            repoDirectory.toPath(),
            UpdateIndexCommand.Options(
                fileName = fileName,
                content = content,
            ),
        ).run()

    suspend fun discard(fileName: String, content: String) =
        vcs.getDiscardCommand(
            repoDirectory.toPath(),
            DiscardCommand.Options(
                fileName = fileName,
                content = content,
            ),
        ).run()
}
