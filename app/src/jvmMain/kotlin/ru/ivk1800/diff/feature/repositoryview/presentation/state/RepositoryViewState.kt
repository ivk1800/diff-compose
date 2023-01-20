package ru.ivk1800.diff.feature.repositoryview.presentation.state

data class RepositoryViewState(
    val sidePanelState: SidePanelState,
    val historyState: HistoryState,
    val fileStatusState: FileStatusState,
    val stashState: StashState,
)