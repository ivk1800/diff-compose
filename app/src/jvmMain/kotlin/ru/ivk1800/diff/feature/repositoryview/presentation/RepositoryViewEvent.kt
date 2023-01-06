package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem

sealed interface RepositoryViewEvent {
    object OnReload : RepositoryViewEvent

    object OpenTerminal : RepositoryViewEvent

    object OpenFinder : RepositoryViewEvent

    data class OnCommitsSelected(val items: ImmutableSet<CommitTableItem.Id.Commit>) : RepositoryViewEvent

    object OnCommitsUnselected : RepositoryViewEvent

    data class OnFilesSelected(val items: Set<CommitFileId>) : RepositoryViewEvent

    object OnLoadMoreCommits : RepositoryViewEvent

    object OnUncommittedChangesSelected : RepositoryViewEvent

    sealed interface UncommittedChanges : RepositoryViewEvent {
        object OnRemoveAllFromStaged : UncommittedChanges

        object OnAddAllToStaged : UncommittedChanges

        data class OnRemoveFilesFromStaged(val ids: Set<CommitFileId>) : UncommittedChanges

        data class OnAddFilesToStaged(val ids: Set<CommitFileId>) : UncommittedChanges

        data class OnStatedFilesSelected(val items: Set<CommitFileId>) : UncommittedChanges

        data class OnUnstatedFilesSelected(val items: Set<CommitFileId>) : UncommittedChanges
    }
}
