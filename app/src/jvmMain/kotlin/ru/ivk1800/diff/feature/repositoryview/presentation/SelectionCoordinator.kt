package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem

class SelectionCoordinator(
    private val commitsTableInteractor: CommitsTableInteractor,
    private val commitInfoInteractor: CommitInfoInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
    private val uncommittedChangesInteractor: UncommittedChangesInteractor,
) {
    // TODO: add main dispatcher
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    init {
        commitInfoInteractor.state
            .onEach {
                diffInfoInteractor.selectLines(persistentSetOf())
            }
            .launchIn(scope)
    }

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
                diffInfoInteractor.onDiffSelected(diffId, DiffInfoInteractor.UncommittedChangesType.Staged)
            }
        }
    }

    fun selectUnstatedFiles(files: Set<CommitFileId>) {
        if (files.size == 1) {
            val selectedFile = files.first()

            val diffId = uncommittedChangesInteractor.getDiffIdOfUnstagedOrNull(selectedFile.path)

            if (diffId != null) {
                diffInfoInteractor.onDiffSelected(diffId, DiffInfoInteractor.UncommittedChangesType.Unstaged)
            }
        }
    }

    fun selectDiffLines(ids: ImmutableSet<DiffInfoItem.Id.Line>) {
        diffInfoInteractor.selectLines(ids)
    }

    fun dispose() {
        scope.cancel()
    }
}
