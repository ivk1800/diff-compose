package ru.ivk1800.diff.feature.repositoryview.presentation.helper

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import ru.ivk1800.diff.feature.repositoryview.domain.CommitFile
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId

class UncommittedChangesNextSelectionHelper {
    fun calculateIndex(allFiles: List<CommitFile>, id: CommitFileId): Int? =
        allFiles.indexOfFirst { it.name == id.path }.takeIf { it != -1 }

    fun confirm(allFiles: List<CommitFile>, removedFileIndex: Int): ImmutableSet<CommitFileId> {
        val file: CommitFile? = allFiles.getOrNull(removedFileIndex)
            ?: allFiles.lastOrNull()
        val fileForSelection = file?.name?.let(::CommitFileId)

        return if (fileForSelection != null) {
            persistentSetOf(fileForSelection)
        } else {
            persistentSetOf()
        }
    }
}
