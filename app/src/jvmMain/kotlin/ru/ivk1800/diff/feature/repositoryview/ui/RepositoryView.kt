package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.ivk1800.diff.feature.repositoryview.presentation.event.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.event.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.event.SidePanelEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.state.RepositoryViewState

@Composable
fun RepositoryView(
    modifier: Modifier = Modifier,
    state: RepositoryViewState,
    onEvent: (value: RepositoryViewEvent) -> Unit,
    onHistoryEvent: (value: HistoryEvent) -> Unit,
    onSidePanelEvent: (value: SidePanelEvent) -> Unit,
) =
    Scaffold(
        modifier = modifier,
        topBar = { AppBar(onEvent) }
    ) {
        Column {
            Body(
                modifier = Modifier.weight(1F),
                state,
                onHistoryEvent,
                onSidePanelEvent,
            )
            CommandsActivityView(state = state.commandsActivityState)
        }
    }

@Composable
private fun Body(
    modifier: Modifier = Modifier,
    state: RepositoryViewState,
    onEvent: (value: HistoryEvent) -> Unit,
    onSidePanelEvent: (value: SidePanelEvent) -> Unit,
) =
    DraggableTwoPanes(
        modifier = modifier,
        orientation = Orientation.Horizontal,
        percent = 15F,
    ) {
        SidePanel(
            modifier = Modifier.fillMaxSize(),
            state.sidePanelState,
            onSidePanelEvent,
        )
        MainContainer(
            modifier = Modifier.fillMaxSize(),
            historyState = state.historyState,
            fileStatusState = state.fileStatusState,
            stashState = state.stashState,
            workspaceState = state.sidePanelState.workspaceState,
            onEvent,
        )
    }
