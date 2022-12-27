package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.ivk1800.diff.feature.repositoryview.presentation.UncommittedChangesState

@Composable
fun UncommittedChangesInfoView(
    modifier: Modifier = Modifier,
    state: UncommittedChangesState,
) = DraggableTwoPanes(
    orientation = Orientation.Vertical,
    percent = 50F,
) {
    when (state) {
        is UncommittedChangesState.Content -> {
            Box(modifier = modifier.fillMaxSize()) {
                CommitFilesListView(
                    modifier = Modifier,
                    items = state.staged,
                    onSelected = { },
                )
            }
            Box(modifier = modifier.fillMaxSize()) {
                CommitFilesListView(
                    modifier = Modifier,
                    items = state.notStaged,
                    onSelected = { },
                )
            }
        }
        UncommittedChangesState.None -> Unit
    }
}
