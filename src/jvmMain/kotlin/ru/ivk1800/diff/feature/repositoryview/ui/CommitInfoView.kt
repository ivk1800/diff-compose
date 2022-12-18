package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoState

@Composable
fun CommitInfoView(
    modifier: Modifier = Modifier,
    state: CommitInfoState,
) = when (state) {
    is CommitInfoState.Content -> {
        CommitFilesListView(
            modifier = modifier,
            items = state.items,
        )
    }

    CommitInfoState.None -> Unit
}
