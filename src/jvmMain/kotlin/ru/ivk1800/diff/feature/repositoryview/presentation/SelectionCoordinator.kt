package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem

class SelectionCoordinator(
    private val commitsTableInteractor: CommitsTableInteractor,
    private val commitInfoInteractor: CommitInfoInteractor,
) {
    fun selectUncommittedChanges() {
        commitsTableInteractor.selectUncommittedChanges()
        commitInfoInteractor.selectCommit(null)
    }

    fun selectCommits(commits: ImmutableSet<CommitTableItem.Id.Commit>) {
        commitsTableInteractor.selectCommits(commits)
        commitInfoInteractor.selectCommit(
            if (commits.size == 1) {
                commits.first().id
            } else {
                null
            }
        )
    }
}
