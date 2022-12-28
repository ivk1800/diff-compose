package ru.ivk1800.diff.feature.repositoryview.presentation

import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitId

sealed interface RepositoryViewEvent {
    object OnReload : RepositoryViewEvent

    object OpenTerminal : RepositoryViewEvent

    object OpenFinder : RepositoryViewEvent

    data class OnCommitsSelected(val items: Set<CommitId>) : RepositoryViewEvent

    object OnCommitsUnselected : RepositoryViewEvent

    data class OnFilesSelected(val items: Set<CommitFileId>) : RepositoryViewEvent

    object OnLoadMoreCommits : RepositoryViewEvent

    object OnUncommittedChangesSelected : RepositoryViewEvent

    sealed interface UncommittedChanges : RepositoryViewEvent {
        object OnRemoveAllFromStaged : UncommittedChanges

        object OnAddAllToStaged : UncommittedChanges

        data class OnRemoveFileFromStaged(val id: CommitFileId) : UncommittedChanges

        data class OnAddFileToStaged(val id: CommitFileId) : UncommittedChanges
    }
}
