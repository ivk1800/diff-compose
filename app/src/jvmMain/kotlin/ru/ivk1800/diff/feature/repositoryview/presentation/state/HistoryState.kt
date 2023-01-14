package ru.ivk1800.diff.feature.repositoryview.presentation.state

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitDescription
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem

@Immutable
data class HistoryState(
    val commitsTableState: CommitsTableState,
    val diffInfoState: DiffInfoState,
    val filesInfoState: FilesInfoState,
)

@Immutable
sealed interface CommitsTableState {
    object Loading : CommitsTableState

    data class Content(
        val selected: ImmutableSet<CommitTableItem.Id>,
        val commits: ImmutableList<CommitTableItem>,
    ) : CommitsTableState
}

@Immutable
sealed interface CommitInfoState {
    object None : CommitInfoState

    data class Error(val message: String) : CommitInfoState

    data class Content(
        val selected: ImmutableSet<CommitFileId>,
        val files: ImmutableList<CommitFileItem>,
        val description: CommitDescription,
    ) : CommitInfoState
}

@Immutable
sealed interface DiffInfoState {
    object None : DiffInfoState

    data class Error(val message: String) : DiffInfoState

    data class Content(
        val selected: ImmutableSet<DiffInfoItem.Id.Line>,
        val items: ImmutableList<DiffInfoItem>,
    ) : DiffInfoState
}

@Immutable
sealed interface UncommittedChangesState {
    object None : UncommittedChangesState

    data class Content(
        val staged: Staged,
        val unstaged: Unstaged,
    ) : UncommittedChangesState {
        @Immutable
        data class Staged(
            val selected: ImmutableSet<CommitFileId>,
            val vcsProcess: Boolean,
            val files: ImmutableList<CommitFileItem>,
        )

        @Immutable
        data class Unstaged(
            val selected: ImmutableSet<CommitFileId>,
            val vcsProcess: Boolean,
            val files: ImmutableList<CommitFileItem>,
        )
    }
}

@Immutable
sealed interface FilesInfoState {
    object None : FilesInfoState

    data class Commit(
        val state: CommitInfoState,
    ) : FilesInfoState

    data class UncommittedChanges(
        val state: UncommittedChangesState.Content,
    ) : FilesInfoState
}
