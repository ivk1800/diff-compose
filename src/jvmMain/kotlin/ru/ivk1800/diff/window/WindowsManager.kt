package ru.ivk1800.diff.window

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WindowsManager {

    private val _state = MutableStateFlow<PersistentList<Window>>(
        persistentListOf(
            Window.Bookmarks,
            Window.Repository("/Users/ivan/repos/compose/diff-compose")
        )
    )
    val state: StateFlow<ImmutableList<Window>>
        get() = _state

    fun openRepositoryWindowIfAbsent(path: String) {
        val isActive = _state.value.filterIsInstance<Window.Repository>().any { it.path == path }

        if (!isActive) {
            _state.value = _state.value.add(Window.Repository(path))
        }
    }

    fun closeRepositoryWindow(path: String) {
        val activeRepositoryWindow = _state.value.filterIsInstance<Window.Repository>().firstOrNull { it.path == path }
        if (activeRepositoryWindow != null) {
            _state.value = _state.value.remove(activeRepositoryWindow)
        }
    }

    @Immutable
    sealed interface Window {
        object Bookmarks : Window
        data class Repository(val path: String) : Window
    }
}

val LocalWindowsManager =
    staticCompositionLocalOf<WindowsManager> { throw IllegalStateException("WindowsManager not provided") }