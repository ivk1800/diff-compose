package ru.ivk1800.diff.feature.preferences

import ru.ivk1800.diff.application.ApplicationThemeProvider
import ru.ivk1800.diff.feature.preferences.presentation.PreferencesRouter

data class PreferencesDependencies(
    val router: PreferencesRouter,
    val themeProvider: ApplicationThemeProvider,
)
