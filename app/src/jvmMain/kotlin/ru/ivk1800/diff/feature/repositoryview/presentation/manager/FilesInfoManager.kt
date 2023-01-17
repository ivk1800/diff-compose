package ru.ivk1800.diff.feature.repositoryview.presentation.manager

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
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitsTableState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FilesInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.UncommittedChangesState

class FilesInfoManager(
    private val commitInfoManager: CommitInfoManager,
    private val uncommittedChangesManager: UncommittedChangesManager,
    private val commitsTableManager: CommitsTableManager,
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
            commitInfoManager.state,
            uncommittedChangesManager.state,
            commitsTableManager.state,
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
