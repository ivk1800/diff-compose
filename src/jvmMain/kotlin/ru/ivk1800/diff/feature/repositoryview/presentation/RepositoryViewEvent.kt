package ru.ivk1800.diff.feature.repositoryview.presentation

sealed interface RepositoryViewEvent {
    object OnReload: RepositoryViewEvent

    object OpenTerminal: RepositoryViewEvent

    object OpenFinder: RepositoryViewEvent

    data class OnCommitsSelected(val range: IntRange): RepositoryViewEvent

    object OnCommitsUnselected: RepositoryViewEvent
}
