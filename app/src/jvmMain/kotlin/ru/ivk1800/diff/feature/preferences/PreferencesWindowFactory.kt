package ru.ivk1800.diff.feature.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import ru.ivk1800.diff.feature.preferences.di.PreferencesWindowScope
import ru.ivk1800.diff.feature.preferences.di.compose.LocalPreferencesWindowScope
import ru.ivk1800.diff.feature.preferences.ui.PreferencesWindow

class PreferencesWindowFactory(
    private val dependencies: PreferencesDependencies,
) {
    @Composable
    fun create() {
        val scope = remember { PreferencesWindowScope(dependencies) }

        DisposableEffect(scope) {
            onDispose { scope.dispose() }
        }

        CompositionLocalProvider(LocalPreferencesWindowScope provides scope) {
            PreferencesWindow()
        }
    }
}
