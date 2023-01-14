package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.command.ShowCommand
import java.io.File

class FileRepository(
    private val repoDirectory: File,
    private val vcs: Vcs,
) {
    suspend fun getFileLines(fileName: String): List<String> =
        vcs.getShowCommand(repoDirectory.toPath(), ShowCommand.Options(fileName = fileName)).run()
}
