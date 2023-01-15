package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.ivk1800.diff.feature.repositoryview.presentation.event.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FileStatusState

@Composable
fun FileStatusView(
    modifier: Modifier = Modifier,
    state: FileStatusState,
    onEvent: (value: HistoryEvent) -> Unit,
) =
    when (state) {
        is FileStatusState.Content -> DraggableTwoPanes(
            modifier = modifier,
            orientation = Orientation.Horizontal,
            percent = 40F,
        ) {
            UncommittedChangesInfoView(
                state = state.uncommittedChangesState,
                onEvent = onEvent,
            )
            DiffInfoView(
                modifier = Modifier.fillMaxSize(),
                state = state.diffInfoState,
                onEvent = onEvent,
            )
        }

        FileStatusState.None -> Empty(modifier)
    }

@Composable
private fun Empty(
    modifier: Modifier = Modifier,
) =
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.h6,
            text = "Nothing to commit",
        )
    }
