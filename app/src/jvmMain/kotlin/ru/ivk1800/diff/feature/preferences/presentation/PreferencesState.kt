package ru.ivk1800.diff.feature.preferences.presentation

import ru.ivk1800.diff.application.ApplicationTheme

sealed interface PreferencesState {
    data class Appearance(val theme: ApplicationTheme): PreferencesState
}
