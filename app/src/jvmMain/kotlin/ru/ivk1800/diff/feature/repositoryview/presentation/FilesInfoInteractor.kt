package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem

class FilesInfoInteractor(
    private val commitInfoInteractor: CommitInfoInteractor,
    private val uncommittedChangesInteractor: UncommittedChangesInteractor,
    private val commitsTableInteractor: CommitsTableInteractor,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val state: StateFlow<FilesInfoState> = getFilesInfoStateFlow()
        .stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = FilesInfoState.None,
        )

    private fun getFilesInfoStateFlow(): Flow<FilesInfoState> =
        combine(
            commitInfoInteractor.state,
            uncommittedChangesInteractor.state,
            commitsTableInteractor.state,
        ) { commitInfo, uncommittedChanges, commitsTable ->
            if (commitInfo !is CommitInfoState.None) {
                FilesInfoState.Commit(commitInfo)
            } else if (uncommittedChanges is UncommittedChangesState.Content &&
                commitsTable is CommitsTableState.Content &&
                commitsTable.selected.contains(CommitTableItem.Id.UncommittedChanges)
            ) {
                FilesInfoState.UncommittedChanges(uncommittedChanges)
            } else {
                FilesInfoState.None
            }
        }

    fun dispose() {
        scope.cancel()
    }
}
