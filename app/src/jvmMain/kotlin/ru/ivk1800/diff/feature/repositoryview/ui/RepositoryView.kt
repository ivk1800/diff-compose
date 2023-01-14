package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.state.HistoryState

@Composable
fun RepositoryView(
    modifier: Modifier = Modifier,
    state: HistoryState,
    onEvent: (value: RepositoryViewEvent) -> Unit,
) =
    Scaffold(
        modifier = modifier,
        topBar = { AppBar(onEvent) }
    ) {
        Body(state, onEvent)
    }

@Composable
private fun Body(
    state: HistoryState,
    onEvent: (value: RepositoryViewEvent) -> Unit,
) =
    DraggableTwoPanes(
        modifier = Modifier,
        orientation = Orientation.Horizontal,
        percent = 15F,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        )
        HistoryView(
            modifier = Modifier.fillMaxSize(),
            state,
            onEvent,
        )
    }
