package ru.ivk1800.diff.feature.repositoryview.presentation.manager

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitsTableState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.UncommittedChangesState

class CommitsTableManager(
    private val commitsManager: CommitsManager,
    private val uncommittedChangesManager: UncommittedChangesManager,
    private val diffInfoManager: DiffInfoManager,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val uncommittedChangesSelected = MutableStateFlow<Boolean>(false)

    private val _state =
        combine(
            commitsManager.state,
            uncommittedChangesManager.state,
            uncommittedChangesSelected,
        ) { commitsTableState, uncommittedChangesState, isUncommittedChangesSelected ->
            when (uncommittedChangesState) {
                is UncommittedChangesState.Content ->
                    when (commitsTableState) {
                        is CommitsTableState.Content -> commitsTableState.copy(
                            selected = if (isUncommittedChangesSelected) {
                                persistentSetOf(CommitTableItem.Id.UncommittedChanges)
                            } else {
                                commitsTableState.selected
                            },
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

    val state: StateFlow<CommitsTableState>
        get() = _state

    fun selectCommits(commits: ImmutableSet<CommitTableItem.Id.Commit>) {
        uncommittedChangesSelected.value = false
        commitsManager.selectCommits(commits)
        diffInfoManager.unselect()
    }

    fun selectUncommittedChanges() {
        uncommittedChangesSelected.value = true
        commitsManager.selectCommits(persistentSetOf())
        diffInfoManager.unselect()
    }

    fun reload() = commitsManager.reload()

    fun loadMore() = commitsManager.loadMore()

    fun dispose() = scope.cancel()
}
