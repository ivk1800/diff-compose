package ru.ivk1800.diff.feature.preferences.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.application.ApplicationTheme
import ru.ivk1800.diff.application.ApplicationThemeProvider
import ru.ivk1800.diff.feature.preferences.presentation.event.PreferencesViewEvent
import ru.ivk1800.diff.presentation.BaseViewModel

class PreferencesViewModel(
    private val themeProvider: ApplicationThemeProvider,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        PreferencesState.Appearance(
            theme = ApplicationTheme.Dark,
        )
    )

    val state: StateFlow<PreferencesState>
        get() = _state

    init {
        themeProvider.theme
            .onEach { theme ->
                _state.value = _state.value.copy(
                    theme = theme,
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: PreferencesViewEvent) {
        when (event) {
            is PreferencesViewEvent.OnThemeSelected -> themeProvider.switch(event.value)
        }
    }
}
