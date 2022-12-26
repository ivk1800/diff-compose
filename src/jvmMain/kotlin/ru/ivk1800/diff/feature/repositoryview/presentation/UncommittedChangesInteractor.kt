package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem

class UncommittedChangesInteractor {
    private val _state = MutableStateFlow<UncommittedChangesState>(
        UncommittedChangesState.None,
    )
    val state: StateFlow<UncommittedChangesState>
        get() = _state

    fun activate() {
        _state.value = UncommittedChangesState.Content(
            files = persistentListOf(
                CommitFileItem(
                    name = "Test.txt",
                    type = CommitFileItem.Type.Added,
                ),
            )
        )
    }

    fun deactivate() {
        _state.value = UncommittedChangesState.None
    }

    fun dispose() {

    }
}
