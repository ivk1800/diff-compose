package ru.ivk1800.diff.feature.repositoryview.presentation.state.composer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.WorkspaceManager
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FileStatusState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.RepositoryViewState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.SidePanelState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.WorkspaceState

class RepositoryViewStateComposer(
    private val historyStateComposer: HistoryStateComposer,
    private val fileStatusStateComposer: FileStatusStateComposer,
    private val workspaceManager: WorkspaceManager,
    private val stashStateComposer: StashStateComposer,
    private val commandsActivityStateComposer: CommandsActivityStateComposer,
) {
    fun getState(scope: CoroutineScope): StateFlow<RepositoryViewState> =
        combine(
            historyStateComposer.getState(scope),
            workspaceManager.state,
            fileStatusStateComposer.getState(scope),
            stashStateComposer.getState(scope),
            commandsActivityStateComposer.getState(scope)
        ) { historyState, workspaceState, fileInfoState, stashState, commandsActivityState ->
            RepositoryViewState(
                sidePanelState = SidePanelState(
                    workspaceState = workspaceState,
                ),
                historyState = historyState,
                fileStatusState = fileInfoState,
                stashState = stashState,
                commandsActivityState = commandsActivityState,
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
                ),
            ),
            historyState = historyStateComposer.getDefaultState(),
            fileStatusState = FileStatusState.None,
            stashState = stashStateComposer.getDefaultState(),
            commandsActivityState = commandsActivityStateComposer.getDefaultState(),
        )
}
