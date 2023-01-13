package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.command.UpdateIndexCommand
import java.io.File

class IndexRepository(
    private val vcs: Vcs,
) {
    suspend fun updateIndex(directory: File, fileName: String, content: String) =
        vcs.getUpdateIndexCommand(
            directory.toPath(),
            UpdateIndexCommand.Options(
                fileName = fileName,
                content = content,
            ),
        ).run()
}
