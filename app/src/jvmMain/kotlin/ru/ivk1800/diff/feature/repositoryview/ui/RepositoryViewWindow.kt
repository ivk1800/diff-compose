package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import ru.ivk1800.diff.feature.repositoryview.di.compose.LocalRepositoryViewWindowScope
import ru.ivk1800.diff.feature.repositoryview.presentation.event.RepositoryViewEvent
import ru.ivk1800.diff.ui.compose.DiffAlertDialog
import ru.ivk1800.diff.ui.compose.rememberWindowKeyEventDelegate

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RepositoryViewWindow() {
    val scope = LocalRepositoryViewWindowScope.current
    val viewModel = scope.repositoryViewViewModel

    val keyEventDelegate = rememberWindowKeyEventDelegate(
        test = { event -> event.isMetaPressed && event.key == Key.R },
        onDown = { viewModel.onEvent(RepositoryViewEvent.OnReload) },
    )

    Window(
        title = scope.id.path,
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 1280.dp, height = 720.dp)
        ),
        onCloseRequest = { scope.router.close(scope.id) },
        onKeyEvent = keyEventDelegate::onKeyEvent,
    ) {

        MenuBar {
            Menu("Preferences") {
                Item(
                    "Preferences",
                    onClick = scope.router::toPreferences,
                )
            }
        }

        val state by viewModel.state.collectAsState()
        RepositoryView(
            state = state,
            onHistoryEvent = viewModel::onHistoryEvent,
            onEvent = viewModel::onEvent,
            onSidePanelEvent = viewModel::onSidePanelEvent,
        )

        val currentDialog by scope.dialogManager.currentDialog.collectAsState()

        val dialog = currentDialog
        if (dialog != null)
            DiffAlertDialog(
                dialog,
                onConfirmClick = {
                    scope.dialogManager.close()
                },
            )
    }
}
