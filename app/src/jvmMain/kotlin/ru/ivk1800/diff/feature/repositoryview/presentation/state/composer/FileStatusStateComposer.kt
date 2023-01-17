package ru.ivk1800.diff.feature.repositoryview.presentation.state.composer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.DiffInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.UncommittedChangesManager
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FileStatusState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.UncommittedChangesState

class FileStatusStateComposer(
    private val uncommittedChangesManager: UncommittedChangesManager,
    private val diffInfoManager: DiffInfoManager,
) {
    fun getState(scope: CoroutineScope): StateFlow<FileStatusState> =
        combine(
            uncommittedChangesManager.state,
            diffInfoManager.state,
        ) {  uncommittedChangesState, diffInfoState ->
            when (uncommittedChangesState) {
                is UncommittedChangesState.Content -> FileStatusState.Content(
                    uncommittedChangesState = uncommittedChangesState,
                    diffInfoState = diffInfoState,
                )

                UncommittedChangesState.None -> FileStatusState.None
            }
        }
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = FileStatusState.None,
            )
}
