package ru.ivk1800.diff.feature.repositoryview.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitDescription
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem

@Immutable
data class RepositoryViewState(
    val commitsTableState: CommitsTableState,
    val diffInfoState: DiffInfoState,
    val activeState: ActiveState,
)

@Immutable
sealed interface CommitsTableState {
    object Loading : CommitsTableState

    data class Content(val commits: ImmutableList<CommitTableItem>) : CommitsTableState
}

@Immutable
sealed interface CommitInfoState {
    object None : CommitInfoState

    data class Content(
        val files: ImmutableList<CommitFileItem>,
        val description: CommitDescription,
    ) : CommitInfoState
}

@Immutable
sealed interface DiffInfoState {
    object None : DiffInfoState

    data class Error(val message: String) : DiffInfoState

    data class Content(val items: ImmutableList<DiffInfoItem>) : DiffInfoState
}

@Immutable
sealed interface UncommittedChangesState {
    object None : UncommittedChangesState

    data class Content(
        val files: ImmutableList<CommitFileItem>,
    ) : UncommittedChangesState
}

@Immutable
sealed interface ActiveState {
    object None : ActiveState

    data class Commit(
        val state: CommitInfoState.Content,
    ) : ActiveState

    data class UncommittedChanges(
        val state: UncommittedChangesState.Content,
    ) : ActiveState
}
