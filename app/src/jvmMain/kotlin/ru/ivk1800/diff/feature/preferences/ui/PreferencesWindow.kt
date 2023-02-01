package ru.ivk1800.diff.feature.preferences.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import ru.ivk1800.diff.feature.preferences.di.PreferencesWindowScope
import ru.ivk1800.diff.feature.preferences.di.compose.LocalPreferencesWindowScope

@Composable
fun PreferencesWindow() {
    val scope: PreferencesWindowScope = LocalPreferencesWindowScope.current

    Window(
        title = "Preferences",
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 720.dp, height = 720.dp)
        ),
        onCloseRequest = scope.router::close,
    ) {
        val state by scope.preferencesViewModel.state.collectAsState()
        PreferencesView(state, scope.preferencesViewModel::onEvent)
    }
}
