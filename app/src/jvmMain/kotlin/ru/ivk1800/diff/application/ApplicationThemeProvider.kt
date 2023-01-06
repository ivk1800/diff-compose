package ru.ivk1800.diff.application

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ApplicationThemeProvider {
    private val _theme = MutableStateFlow(ApplicationTheme.Dark)
    val theme: StateFlow<ApplicationTheme>
        get() = _theme

    fun switch(newTheme: ApplicationTheme) {
        _theme.value = newTheme
    }
}

