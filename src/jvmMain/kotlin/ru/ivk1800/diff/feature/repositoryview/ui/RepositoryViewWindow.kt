package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState

@Composable
fun RepositoryViewWindow(path: String, onCloseRequest: () -> Unit) {
    Window(
        title = path,
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 1280.dp, height = 720.dp)
        ),
        onCloseRequest = onCloseRequest,
    ) {
        Text(text = path)
    }
}