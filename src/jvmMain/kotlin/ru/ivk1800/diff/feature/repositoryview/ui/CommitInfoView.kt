package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoState

@Composable
fun CommitInfoView(
    modifier: Modifier = Modifier,
    state: CommitInfoState,
    onFilesSelected: (event: SelectEvent) -> Unit
) = Box(modifier = modifier) {
    when (state) {
        is CommitInfoState.Content -> {
            CommitFilesListView(
                items = state.items,
                onSelected = onFilesSelected,
            )
        }

        CommitInfoState.None -> Unit
    }
}
