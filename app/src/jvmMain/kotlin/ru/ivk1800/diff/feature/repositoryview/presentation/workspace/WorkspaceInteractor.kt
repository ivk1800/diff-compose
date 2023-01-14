package ru.ivk1800.diff.feature.repositoryview.presentation.workspace

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.ivk1800.diff.feature.repositoryview.presentation.state.WorkspaceState

class WorkspaceInteractor {
    private val _state = MutableStateFlow(
        WorkspaceState(
            activeSection = WorkspaceState.Section.History,
        )
    )
    val state: StateFlow<WorkspaceState>
        get() = _state

    fun selectSection(value: WorkspaceState.Section) {
        _state.value = _state.value.copy(
            activeSection = value,
        )
    }
}
