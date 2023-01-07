package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import java.io.File

class FileRepository(
    private val vcs: Vcs,
) {
    suspend fun getFileLines(directory: File, diffId: String): List<String> = vcs.getContent(directory, diffId)
}
