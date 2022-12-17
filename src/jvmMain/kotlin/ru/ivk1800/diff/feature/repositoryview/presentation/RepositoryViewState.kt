package ru.ivk1800.diff.feature.repositoryview.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface RepositoryViewState {
    object Loading: RepositoryViewState

    data class Content(val commits: ImmutableList<CommitItem>): RepositoryViewState
}
