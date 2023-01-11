package ru.ivk1800.diff.feature.repositoryview.presentation.helper

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId

class UncommittedChangesNextSelectionHelper {
    fun calculateIndex(allFiles: List<String>, id: CommitFileId): Int? =
        allFiles.indexOfFirst { it == id.path }.takeIf { it != -1 }

    fun confirm(allFiles: List<String>, removedFileIndex: Int): ImmutableSet<CommitFileId> {
        val name: String? = allFiles.getOrNull(removedFileIndex)
            ?: allFiles.lastOrNull()
        val fileForSelection = name?.let(::CommitFileId)

        return if (fileForSelection != null) {
            persistentSetOf(fileForSelection)
        } else {
            persistentSetOf()
        }
    }
}
