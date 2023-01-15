package ru.ivk1800.diff.feature.repositoryview.presentation.state.composer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FileStatusState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.RepositoryViewState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.SidePanelState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.WorkspaceState
import ru.ivk1800.diff.feature.repositoryview.presentation.workspace.WorkspaceInteractor

class RepositoryViewStateComposer(
    private val historyStateComposer: HistoryStateComposer,
    private val fileStatusStateComposer: FileStatusStateComposer,
    private val workspaceInteractor: WorkspaceInteractor,
) {
    fun getState(scope: CoroutineScope): StateFlow<RepositoryViewState> =
        combine(
            historyStateComposer.getState(scope),
            workspaceInteractor.state,
            fileStatusStateComposer.getState(scope),
        ) { historyState, workspaceState, fileInfoState ->
            RepositoryViewState(
                sidePanelState = SidePanelState(
                    workspaceState = workspaceState,
                ),
                historyState = historyState,
                fileStatusState = fileInfoState,
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
            fileStatusState = FileStatusState.None,
        )
}
