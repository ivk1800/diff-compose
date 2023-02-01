package ru.ivk1800.diff.feature.preferences.di

import ru.ivk1800.diff.feature.preferences.PreferencesDependencies
import ru.ivk1800.diff.feature.preferences.presentation.PreferencesRouter
import ru.ivk1800.diff.feature.preferences.presentation.PreferencesViewModel

class PreferencesWindowScope(
    private val dependencies: PreferencesDependencies,
) {
    val router: PreferencesRouter
        get() = dependencies.router

    val preferencesViewModel: PreferencesViewModel by lazy {
        PreferencesViewModel(
            themeProvider = dependencies.themeProvider,
        )
    }

    fun dispose() {
        preferencesViewModel.dispose()
    }
}
