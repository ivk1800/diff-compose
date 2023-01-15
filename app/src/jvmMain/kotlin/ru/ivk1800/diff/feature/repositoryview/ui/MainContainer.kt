package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.ivk1800.diff.feature.repositoryview.presentation.event.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FileStatusState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.HistoryState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.WorkspaceState

@Composable
fun MainContainer(
    modifier: Modifier = Modifier,
    historyState: HistoryState,
    fileStatusState: FileStatusState,
    workspaceState: WorkspaceState,
    onEvent: (value: HistoryEvent) -> Unit,
) =
    Box(modifier) {
        HistoryView(
            modifier = Modifier.fillMaxSize(),
            historyState,
            onEvent,
        )
        AnimatedVisibility(
            visible = workspaceState.activeSection == WorkspaceState.Section.FileStatus,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            FileStatusView(
                state = fileStatusState,
                onEvent = onEvent,
            )
        }
    }
