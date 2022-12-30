package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem

class SelectionCoordinator {
    private val _state = MutableStateFlow<State>(
        State(
            selectedCommits = persistentSetOf(),
            commitInfo = null,
            commitDiff = null,
        )
    )
    val state: StateFlow<State>
        get() = _state

    fun selectCommits(commits: ImmutableSet<CommitTableItem.Id.Commit>) {
        _state.value = _state.value.copy(
            selectedCommits = commits,
            commitInfo = if (commits.size == 1) {
                commits.first().id
            } else {
                null
            },
        )
    }

    data class State(
        val selectedCommits: ImmutableSet<CommitTableItem.Id.Commit>,
        val commitInfo: CommitId?,
        val commitDiff: CommitDiff?,
    ) {
        data class CommitDiff(val id: CommitId, val path: String)
    }
}
