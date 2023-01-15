package ru.ivk1800.diff.feature.repositoryview.presentation.state.composer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.UncommittedChangesInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FileStatusState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.UncommittedChangesState

class FileStatusStateComposer(
    private val uncommittedChangesInteractor: UncommittedChangesInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
) {
    fun getState(scope: CoroutineScope): StateFlow<FileStatusState> =
        combine(
            uncommittedChangesInteractor.state,
            diffInfoInteractor.state,
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
