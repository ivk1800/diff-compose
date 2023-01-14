package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ru.ivk1800.diff.feature.repositoryview.presentation.event.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.state.HistoryState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.WorkspaceState

@Composable
fun MainContainer(
    modifier: Modifier = Modifier,
    state: HistoryState,
    workspaceState: WorkspaceState,
    onEvent: (value: HistoryEvent) -> Unit,
) =
    Box {
        HistoryView(
            modifier = Modifier.fillMaxSize(),
            state,
            onEvent,
        )
        AnimatedVisibility(
            visible = workspaceState.activeSection == WorkspaceState.Section.FileStatus,
            modifier = modifier,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(Modifier.background(Color.Gray)) { }
        }
    }
