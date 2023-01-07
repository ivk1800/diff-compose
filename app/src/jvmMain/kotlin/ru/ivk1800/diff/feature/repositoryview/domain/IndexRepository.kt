package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import java.io.File

class IndexRepository(
    private val vcs: Vcs,
) {
    suspend fun updateIndex(directory: File, fileName: String, id: String) = vcs.updateIndex(directory, fileName, id)
}
