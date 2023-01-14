package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.ivk1800.diff.feature.repositoryview.presentation.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.state.RepositoryViewState

@Composable
fun RepositoryView(
    modifier: Modifier = Modifier,
    state: RepositoryViewState,
    onEvent: (value: HistoryEvent) -> Unit,
) =
    Scaffold(
        modifier = modifier,
        topBar = { AppBar(onEvent) }
    ) {
        Body(state, onEvent)
    }

@Composable
private fun Body(
    state: RepositoryViewState,
    onEvent: (value: HistoryEvent) -> Unit,
) =
    DraggableTwoPanes(
        modifier = Modifier,
        orientation = Orientation.Horizontal,
        percent = 15F,
    ) {
        SidePanel(
            modifier = Modifier.fillMaxSize(),
            state.sidePanelState,
        )
        HistoryView(
            modifier = Modifier.fillMaxSize(),
            state.historyState,
            onEvent,
        )
    }
