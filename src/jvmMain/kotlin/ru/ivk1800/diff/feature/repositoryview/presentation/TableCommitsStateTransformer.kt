package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem

class TableCommitsStateTransformer {
    fun transform(
        scope: CoroutineScope,
        commitsTableStateFlow: StateFlow<CommitsTableState>,
        uncommittedChangesStateFlow: StateFlow<UncommittedChangesState>,
    ): StateFlow<CommitsTableState> {
        return commitsTableStateFlow.combine(uncommittedChangesStateFlow) { commitsTableState, uncommittedChangesState ->
            when (uncommittedChangesState) {
                is UncommittedChangesState.Content ->
                    when (commitsTableState) {
                        is CommitsTableState.Content -> commitsTableState.copy(
                            commits = commitsTableState.commits.toPersistentList().mutate {
                                it.add(
                                    0,
                                    CommitTableItem.UncommittedChanges,
                                )
                            }
                        )

                        CommitsTableState.Loading -> commitsTableState
                    }

                UncommittedChangesState.None -> commitsTableState
            }
        }.stateIn(
            scope = scope,
            started = SharingStarted.Lazily,
            initialValue = CommitsTableState.Loading,
        )
    }
}
