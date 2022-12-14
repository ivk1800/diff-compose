package ru.ivk1800.diff.feature.bookmarks.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState

@Composable
fun BookmarksWindow() {
    Window(
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 480.dp, height = 640.dp)
        ),
        onCloseRequest = {},
    ) {
        BookmarksView()
    }
}