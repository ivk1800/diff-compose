package ru.ivk1800.diff.feature.repositoryview.presentation.event

import ru.ivk1800.diff.feature.repositoryview.presentation.state.WorkspaceState

sealed interface SidePanelEvent {
    data class OnSectionUnselected(val value: WorkspaceState.Section) : SidePanelEvent
}
