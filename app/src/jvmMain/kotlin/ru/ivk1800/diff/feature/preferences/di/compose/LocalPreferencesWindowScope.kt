package ru.ivk1800.diff.feature.preferences.di.compose

import androidx.compose.runtime.staticCompositionLocalOf
import ru.ivk1800.diff.feature.preferences.di.PreferencesWindowScope

val LocalPreferencesWindowScope = staticCompositionLocalOf<PreferencesWindowScope> {
    error("LocalPreferencesWindowScope not provided")
}
