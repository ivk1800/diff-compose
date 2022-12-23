package ru.ivk1800.diff.feature.repositoryview.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem

@Immutable
data class RepositoryViewState(
    val commitsTableState: CommitsTableState,
    val commitInfoState: CommitInfoState,
    val diffInfoState: DiffInfoState,
)

@Immutable
sealed interface CommitsTableState {
    object Loading: CommitsTableState

    data class Content(val commits: ImmutableList<CommitItem>): CommitsTableState
}

@Immutable
sealed interface CommitInfoState {
    object None : CommitInfoState

    data class Content(val items: ImmutableList<CommitFileItem>): CommitInfoState
}

@Immutable
sealed interface DiffInfoState {
    object None : DiffInfoState

    data class Content(val items: ImmutableList<DiffInfoItem>): DiffInfoState
}