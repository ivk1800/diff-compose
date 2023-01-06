package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem

class SelectionCoordinator(
    private val commitsTableInteractor: CommitsTableInteractor,
    private val commitInfoInteractor: CommitInfoInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
    private val uncommittedChangesInteractor: UncommittedChangesInteractor,
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

    fun selectStatedFiles(files: Set<CommitFileId>) {
        if (files.size == 1) {
            val selectedFile = files.first()

            val diffId = uncommittedChangesInteractor.getDiffIdOfStagedOrNull(selectedFile.path)

            if (diffId != null) {
                diffInfoInteractor.onDiffSelected(diffId)
            }
        }
    }

    fun selectUnstatedFiles(files: Set<CommitFileId>) {
        if (files.size == 1) {
            val selectedFile = files.first()

            val diffId = uncommittedChangesInteractor.getDiffIdOfUnstagedOrNull(selectedFile.path)

            if (diffId != null) {
                diffInfoInteractor.onDiffSelected(diffId)
            }
        }
    }
}
