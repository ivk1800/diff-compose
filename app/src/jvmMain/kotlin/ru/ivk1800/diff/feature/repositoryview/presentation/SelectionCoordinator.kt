package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import kotlin.coroutines.CoroutineContext

class SelectionCoordinator internal constructor(
    private val commitsTableInteractor: CommitsTableInteractor,
    private val commitInfoInteractor: CommitInfoInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
    private val uncommittedChangesInteractor: UncommittedChangesInteractor,
    context: CoroutineContext,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    constructor(
        commitsTableInteractor: CommitsTableInteractor,
        commitInfoInteractor: CommitInfoInteractor,
        diffInfoInteractor: DiffInfoInteractor,
        uncommittedChangesInteractor: UncommittedChangesInteractor,
    ) : this(
        commitsTableInteractor,
        commitInfoInteractor,
        diffInfoInteractor,
        uncommittedChangesInteractor,
        Dispatchers.Main,
    )

    init {
        commitInfoInteractor.state
            .onEach {
                diffInfoInteractor.selectLines(persistentSetOf())
            }
            .launchIn(scope)
        listenSelectedCommits()
    }

    fun selectCommitFiles(items: ImmutableSet<CommitFileId>) {
        if (items.size == 1) {
            commitInfoInteractor.selectFiles(items)
            diffInfoInteractor.onFileSelected(
                commitHash = requireNotNull(commitInfoInteractor.selectedCommitHash),
                path = items.first().path,
            )
        } else {
            commitInfoInteractor.selectFiles(persistentSetOf())
            diffInfoInteractor.onFileUnselected()
        }
    }

    fun selectUncommittedChanges() {
        commitsTableInteractor.selectUncommittedChanges()
        commitInfoInteractor.selectCommit(null)
    }

    fun selectCommits(commits: ImmutableSet<CommitTableItem.Id.Commit>) {
        commitsTableInteractor.selectCommits(commits)
    }

    fun selectStatedFiles(files: ImmutableSet<CommitFileId>) {
        uncommittedChangesInteractor.selectStatedFiles(files)
        if (files.size == 1) {
            val selectedFile = files.first()

            val diffId = uncommittedChangesInteractor.getDiffIdOfStagedOrNull(selectedFile.path)

            if (diffId != null) {
                diffInfoInteractor.selectUncommittedFiles(
                    selectedFile.path,
                    DiffInfoInteractor.UncommittedChangesType.Staged,
                )
            }
        }
    }

    fun selectUnstatedFiles(files: ImmutableSet<CommitFileId>) {
        uncommittedChangesInteractor.selectUnstatedFiles(files)
        if (files.size == 1) {
            val selectedFile = files.first()

            val diffId = uncommittedChangesInteractor.getDiffIdOfUnstagedOrNull(selectedFile.path)

            if (diffId != null) {
                diffInfoInteractor.selectUncommittedFiles(
                    selectedFile.path,
                    DiffInfoInteractor.UncommittedChangesType.Unstaged,
                )
            }
        }
    }

    fun selectDiffLines(ids: ImmutableSet<DiffInfoItem.Id.Line>) {
        diffInfoInteractor.selectLines(ids)
    }

    fun dispose() {
        scope.cancel()
    }

    private fun listenSelectedCommits() {
        commitsTableInteractor.state.map { state ->
            when (state) {
                is CommitsTableState.Content -> state.selected
                CommitsTableState.Loading -> persistentSetOf()
            }
        }
            .map { it.filterIsInstance<CommitTableItem.Id.Commit>() }
            .onEach { selectedCommits ->
                commitInfoInteractor.selectCommit(
                    if (selectedCommits.size == 1) {
                        selectedCommits.first().id
                    } else {
                        null
                    }
                )
            }
            .launchIn(scope)
    }
}
