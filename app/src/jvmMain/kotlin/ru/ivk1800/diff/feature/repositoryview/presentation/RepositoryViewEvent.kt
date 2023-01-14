package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem

sealed interface RepositoryViewEvent {
    object OnReload : RepositoryViewEvent

    object OpenTerminal : RepositoryViewEvent

    object OpenFinder : RepositoryViewEvent

    data class OnCommitsSelected(val items: ImmutableSet<CommitTableItem.Id.Commit>) : RepositoryViewEvent

    object OnCommitsUnselected : RepositoryViewEvent

    data class OnFilesSelected(val items: ImmutableSet<CommitFileId>) : RepositoryViewEvent

    object OnLoadMoreCommits : RepositoryViewEvent

    object OnUncommittedChangesSelected : RepositoryViewEvent

    sealed interface UncommittedChanges : RepositoryViewEvent {
        object OnRemoveAllFromStaged : UncommittedChanges

        object OnAddAllToStaged : UncommittedChanges

        data class OnRemoveFilesFromStaged(val ids: ImmutableSet<CommitFileId>) : UncommittedChanges

        data class OnAddFilesToStaged(val ids: ImmutableSet<CommitFileId>) : UncommittedChanges

        data class OnStatedFilesSelected(val items: ImmutableSet<CommitFileId>) : UncommittedChanges

        data class OnUnstatedFilesSelected(val items: ImmutableSet<CommitFileId>) : UncommittedChanges
    }

    sealed interface Diff: RepositoryViewEvent {
        data class OnLinesSelected(val ids: ImmutableSet<DiffInfoItem.Id.Line>): Diff

        data class OnUnstageHunk(val hunkId: DiffInfoItem.Id.Hunk): Diff

        data class OnDiscardHunk(val hunkId: DiffInfoItem.Id.Hunk): Diff
    }
}
