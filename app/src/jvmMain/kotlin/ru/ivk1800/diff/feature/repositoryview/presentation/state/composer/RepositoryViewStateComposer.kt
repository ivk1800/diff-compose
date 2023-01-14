package ru.ivk1800.diff.feature.repositoryview.presentation.state.composer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.state.RepositoryViewState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.SidePanelState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.WorkspaceState

class RepositoryViewStateComposer(
    private val historyStateComposer: HistoryStateComposer,
) {
    fun getState(scope: CoroutineScope): StateFlow<RepositoryViewState> =
        historyStateComposer.getState(scope).map { historyState ->
            RepositoryViewState(
                sidePanelState = SidePanelState(
                    workspaceState = WorkspaceState(
                        activeSection = WorkspaceState.Section.History,
                    )
                ),
                historyState = historyState,
            )
        }
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = getDefaultState(),
            )

    fun getDefaultState(): RepositoryViewState =
        RepositoryViewState(
            sidePanelState = SidePanelState(
                workspaceState = WorkspaceState(
                    activeSection = WorkspaceState.Section.History,
                )
            ),
            historyState = historyStateComposer.getDefaultState(),
        )
}
