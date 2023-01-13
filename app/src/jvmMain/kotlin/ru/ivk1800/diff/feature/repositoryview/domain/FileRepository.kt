package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.command.ShowCommand
import java.io.File

class FileRepository(
    private val vcs: Vcs,
) {
    suspend fun getFileLines(directory: File, fileName: String): List<String> =
        vcs.getShowCommand(directory.toPath(), ShowCommand.Options(fileName = fileName)).run()
}
