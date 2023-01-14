package ru.ivk1800.diff.feature.repositoryview.presentation.event

interface RepositoryViewEvent {
    object OnReload : RepositoryViewEvent

    object OpenTerminal : RepositoryViewEvent

    object OpenFinder : RepositoryViewEvent
}
