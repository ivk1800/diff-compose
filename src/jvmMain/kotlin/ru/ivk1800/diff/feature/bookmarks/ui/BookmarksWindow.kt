package ru.ivk1800.diff.feature.bookmarks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import ru.ivk1800.arch.presentation.viewModel
import ru.ivk1800.diff.feature.bookmarks.di.compose.LocalBookmarksWindowScope

@Composable
fun BookmarksWindow(onCloseRequest: () -> Unit) {
    Window(
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 480.dp, height = 640.dp)
        ),
        onCloseRequest = onCloseRequest,
    ) {
        val bookmarksWindowScope = LocalBookmarksWindowScope.current

        val viewModel = viewModel { bookmarksWindowScope.bookmarksViewModel }
        val state by viewModel.state.collectAsState()
        BookmarksView(
            state,
            onEvent = viewModel::onEvent,
        )
    }
}