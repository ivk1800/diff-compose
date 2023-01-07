package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import java.io.File

class ObjectRepository(
    private val vcs: Vcs,
) {
    suspend fun writeToDatabase(directory: File, content: String): String = vcs.writeToDatabase(directory, content)
}
