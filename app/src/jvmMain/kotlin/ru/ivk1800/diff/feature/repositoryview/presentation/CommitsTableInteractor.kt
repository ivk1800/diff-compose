package ru.ivk1800.diff.feature.repositoryview.presentation

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

class CommitsTableInteractor(
    private val commitsInteractor: CommitsInteractor,
    private val uncommittedChangesInteractor: UncommittedChangesInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val uncommittedChangesSelected = MutableStateFlow<Boolean>(false)

    private val _state =
        combine(
            commitsInteractor.state,
            uncommittedChangesInteractor.state,
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
        commitsInteractor.selectCommits(commits)
        diffInfoInteractor.unselect()
    }

    fun selectUncommittedChanges() {
        uncommittedChangesSelected.value = true
        commitsInteractor.selectCommits(persistentSetOf())
        diffInfoInteractor.unselect()
    }

    fun reload() = commitsInteractor.reload()

    fun loadMore() = commitsInteractor.loadMore()

    fun dispose() = scope.cancel()
}
