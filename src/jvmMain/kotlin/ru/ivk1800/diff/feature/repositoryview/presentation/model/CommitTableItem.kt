package ru.ivk1800.diff.feature.repositoryview.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface CommitTableItem {
    data class Commit(
        val id: CommitId,
        val description: String,
        val commit: String,
        val author: String,
        val date: String,
    ) : CommitTableItem

    object UncommittedChanges : CommitTableItem

    @Immutable
    sealed interface Id {
        data class Commit(
            val id: CommitId,
        ) : Id

        object UncommittedChanges : Id
    }
}
