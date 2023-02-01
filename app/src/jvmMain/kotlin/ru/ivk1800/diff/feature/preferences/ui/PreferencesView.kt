package ru.ivk1800.diff.feature.preferences.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.ivk1800.diff.application.ApplicationTheme
import ru.ivk1800.diff.feature.preferences.presentation.PreferencesState
import ru.ivk1800.diff.feature.preferences.presentation.event.PreferencesViewEvent

@Composable
fun PreferencesView(
    state: PreferencesState,
    onEvent: (value: PreferencesViewEvent) -> Unit,
) {
    Scaffold {
        Body(state, onEvent)
    }
}

@Composable
private fun Body(
    state: PreferencesState,
    onEvent: (value: PreferencesViewEvent) -> Unit,
) {
    when (state) {
        is PreferencesState.Appearance -> {
            Column {
                Row {
                    Text("Theme:")
                    Spacer(modifier = Modifier.width(8.dp))
                    ThemeDropdown(
                        state.theme,
                        onSelected = { newTheme ->
                            onEvent.invoke(PreferencesViewEvent.OnThemeSelected(newTheme))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeDropdown(currentTheme: ApplicationTheme, onSelected: (theme: ApplicationTheme) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val items by remember { mutableStateOf(ApplicationTheme.values().toList()) }
    Box {
        Text(
            currentTheme.toString(),
            modifier = Modifier
                .clickable(
                    onClick = {
                        expanded = true
                    }
                )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    onClick = {
                        onSelected.invoke(items[index])
                        expanded = false
                    },
                ) {
                    Text(text = item.toString())
                }
            }
        }
    }
}
