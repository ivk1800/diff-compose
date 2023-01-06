package ru.ivk1800.diff.window

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.ivk1800.diff.feature.repositoryview.RepositoryId

class WindowsManager {

    private val _state = MutableStateFlow(
        persistentListOf(
            Window.Bookmarks,
            Window.Repository(RepositoryId("/Users/ivan/repos/compose/diff-compose"))
        )
    )
    val state: StateFlow<ImmutableList<Window>>
        get() = _state

    fun openRepositoryWindowIfAbsent(id: RepositoryId) {
        val isActive = _state.value.filterIsInstance<Window.Repository>().any { it.id == id }

        if (!isActive) {
            _state.value = _state.value.add(Window.Repository(id))
        }
    }

    fun closeRepositoryWindow(id: RepositoryId) {
        val activeRepositoryWindow = _state.value.filterIsInstance<Window.Repository>().firstOrNull { it.id == id }
        if (activeRepositoryWindow != null) {
            _state.value = _state.value.remove(activeRepositoryWindow)
        }
    }

    @Immutable
    sealed interface Window {
        object Bookmarks : Window
        data class Repository(val id: RepositoryId) : Window
    }
}
