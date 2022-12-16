package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import kotlinx.collections.immutable.toImmutableList
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitItem

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

        RepositoryView(
            remember {
                List(100) {
                    CommitItem(
                        description = "Add windows manager",
                        commit = "6d924ad",
                        author = "Ivan <ivan@ivk1800.ru>",
                        date = "16 Dec 2022, 21:22",
                    )
                }.toImmutableList()
            }
        )
    }
}