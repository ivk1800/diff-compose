package ru.ivk1800.diff.feature.repositoryview.presentation.state

data class SidePanelState(val workspaceState: WorkspaceState)

data class WorkspaceState(val activeSection: Section) {
    enum class Section {
        History,
        FileStatus,
    }
}
