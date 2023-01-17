package ru.ivk1800.diff.feature.repositoryview.presentation.event

import kotlinx.collections.immutable.ImmutableSet
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem

sealed interface HistoryEvent {
    data class OnCommitsSelected(val items: ImmutableSet<CommitTableItem.Id.Commit>) : HistoryEvent

    object OnCommitsUnselected : HistoryEvent

    data class OnFilesSelected(val items: ImmutableSet<CommitFileId>) : HistoryEvent

    object OnLoadMoreCommits : HistoryEvent

    object OnUncommittedChangesSelected : HistoryEvent

    sealed interface UncommittedChanges : HistoryEvent {
        object OnRemoveAllFromStaged : UncommittedChanges

        object OnAddAllToStaged : UncommittedChanges

        data class OnRemoveFilesFromStaged(val ids: ImmutableSet<CommitFileId>) : UncommittedChanges

        data class OnAddFilesToStaged(val ids: ImmutableSet<CommitFileId>) : UncommittedChanges

        data class OnStatedFilesSelected(val items: ImmutableSet<CommitFileId>) : UncommittedChanges

        data class OnUnstatedFilesSelected(val items: ImmutableSet<CommitFileId>) : UncommittedChanges
    }

    sealed interface Diff: HistoryEvent {
        data class OnLinesSelected(val ids: ImmutableSet<DiffInfoItem.Id.Line>): Diff

        data class OnUnstageHunk(val hunkId: DiffInfoItem.Id.Hunk): Diff

        data class OnStageHunk(val hunkId: DiffInfoItem.Id.Hunk): Diff

        data class OnDiscardHunk(val hunkId: DiffInfoItem.Id.Hunk): Diff
    }
}
