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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import ru.ivk1800.diff.feature.repositoryview.di.compose.LocalRepositoryViewWindowScope
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEvent
import ru.ivk1800.diff.presentation.viewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RepositoryViewWindow() {
    val scope = LocalRepositoryViewWindowScope.current
    val viewModel = viewModel { scope.repositoryViewViewModel }

    Window(
        title = scope.id.path,
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 1280.dp, height = 720.dp)
        ),
        onCloseRequest = { scope.router.close(scope.id) },
        onKeyEvent = { event ->
            if (event.isMetaPressed && event.key == Key.R) {
                viewModel.onEvent(RepositoryViewEvent.OnReload)
                true
            } else {
                false
            }
        }
    ) {

        val state by viewModel.state.collectAsState()
        RepositoryView(state, onEvent = viewModel::onEvent)
    }
}
