package ru.ivk1800.diff.feature.repositoryview.presentation

import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitId

sealed interface RepositoryViewEvent {
    object OnReload : RepositoryViewEvent

    object OpenTerminal : RepositoryViewEvent

    object OpenFinder : RepositoryViewEvent

    data class OnCommitsSelected(val items: Set<CommitId>) : RepositoryViewEvent

    object OnCommitsUnselected : RepositoryViewEvent

    data class OnFilesSelected(val range: IntRange) : RepositoryViewEvent

    object OnFilesUnselected : RepositoryViewEvent

    object OnLoadMoreCommits : RepositoryViewEvent

    object OnUncommittedChangesSelected: RepositoryViewEvent
}
