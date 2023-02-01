package ru.ivk1800.diff.feature.preferences.presentation.event

import ru.ivk1800.diff.application.ApplicationTheme

sealed interface PreferencesViewEvent {
    data class OnThemeSelected(val value: ApplicationTheme): PreferencesViewEvent
}
