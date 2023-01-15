package ru.ivk1800.diff.feature.repositoryview.presentation.state

sealed interface FileStatusState {
    data class Content(
        val uncommittedChangesState: UncommittedChangesState,
        val diffInfoState: DiffInfoState,
    ) : FileStatusState

    object None : FileStatusState
}
