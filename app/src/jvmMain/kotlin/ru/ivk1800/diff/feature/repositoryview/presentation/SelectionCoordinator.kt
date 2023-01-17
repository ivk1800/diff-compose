package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitsTableManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.DiffInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.UncommittedChangesManager
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitsTableState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.UncommittedChangesState
import kotlin.coroutines.CoroutineContext

class SelectionCoordinator internal constructor(
    private val commitsTableManager: CommitsTableManager,
    private val commitInfoManager: CommitInfoManager,
    private val diffInfoManager: DiffInfoManager,
    private val uncommittedChangesManager: UncommittedChangesManager,
    context: CoroutineContext,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    constructor(
        commitsTableManager: CommitsTableManager,
        commitInfoManager: CommitInfoManager,
        diffInfoManager: DiffInfoManager,
        uncommittedChangesManager: UncommittedChangesManager,
    ) : this(
        commitsTableManager,
        commitInfoManager,
        diffInfoManager,
        uncommittedChangesManager,
        Dispatchers.Main,
    )

    init {
        listenCommitInfo()
        listenSelectedCommits()
        listenSelectedCommitFiles()
        listenUncommittedChanges()
    }

    fun selectCommitFiles(items: ImmutableSet<CommitFileId>) {
        commitInfoManager.selectFiles(items)
    }

    fun selectUncommittedChanges() {
        commitsTableManager.selectUncommittedChanges()
        commitInfoManager.selectCommit(null)
    }

    fun selectCommits(commits: ImmutableSet<CommitTableItem.Id.Commit>) {
        commitsTableManager.selectCommits(commits)
    }

    fun selectStatedFiles(files: ImmutableSet<CommitFileId>) {
        uncommittedChangesManager.selectStatedFiles(files)
    }

    fun selectUnstatedFiles(files: ImmutableSet<CommitFileId>) {
        uncommittedChangesManager.selectUnstatedFiles(files)
    }

    fun selectDiffLines(ids: ImmutableSet<DiffInfoItem.Id.Line>) {
        diffInfoManager.selectLines(ids)
    }

    fun dispose() {
        scope.cancel()
    }

    private fun listenSelectedCommits() {
        commitsTableManager.state.map { state ->
            when (state) {
                is CommitsTableState.Content -> state.selected
                CommitsTableState.Loading -> persistentSetOf()
            }
        }
            .map { it.filterIsInstance<CommitTableItem.Id.Commit>() }
            .onEach { selectedCommits ->
                commitInfoManager.selectCommit(
                    if (selectedCommits.size == 1) {
                        selectedCommits.first().id
                    } else {
                        null
                    }
                )
            }
            .launchIn(scope)
    }

    private fun listenSelectedCommitFiles() {
        commitInfoManager.state.map {
            when (it) {
                is CommitInfoState.Content -> it.selected
                is CommitInfoState.Error,
                CommitInfoState.None -> persistentSetOf()
            }
        }.onEach { selected ->
            if (selected.size == 1) {
                diffInfoManager.onFileSelected(
                    commitHash = requireNotNull(commitInfoManager.selectedCommitHash),
                    path = selected.first().path,
                )
            } else {
                diffInfoManager.unselect()
            }
        }.launchIn(scope)
    }

    private fun listenCommitInfo() {
        commitInfoManager.state
            .onEach {
                diffInfoManager.selectLines(persistentSetOf())
            }
            .launchIn(scope)
    }

    private fun listenUncommittedChanges() {
        uncommittedChangesManager.state.map { state ->
            when (state) {
                is UncommittedChangesState.Content -> {
                    check(!(state.staged.selected.isNotEmpty() && state.unstaged.selected.isNotEmpty())) {
                        "Staged and unstaged cannot be selected at the same time"
                    }
                    when {
                        state.staged.selected.isNotEmpty() -> UncommittedChangesType.Staged to state.staged.selected
                        state.unstaged.selected.isNotEmpty() ->
                            UncommittedChangesType.Unstaged to state.unstaged.selected
                        else -> UncommittedChangesType.None to persistentSetOf()
                    }
                }

                UncommittedChangesState.None -> UncommittedChangesType.None to persistentSetOf()
            }
        }
            // TODO: add test for distinct
            .distinctUntilChanged()
            .onEach { (type, selected) ->
                when (type) {
                    UncommittedChangesType.None -> diffInfoManager.unselect()
                    UncommittedChangesType.Staged -> {
                        if (selected.size == 1) {
                            val selectedFile = selected.first()
                            diffInfoManager.selectUncommittedFiles(
                                selectedFile.path,
                                DiffInfoManager.UncommittedChangesType.Staged,
                            )
                        } else {
                            diffInfoManager.unselect()
                        }
                    }

                    UncommittedChangesType.Unstaged -> {
                        if (selected.size == 1) {
                            val selectedFile = selected.first()
                            diffInfoManager.selectUncommittedFiles(
                                selectedFile.path,
                                DiffInfoManager.UncommittedChangesType.Unstaged,
                            )
                        } else {
                            diffInfoManager.unselect()
                        }
                    }
                }
            }
            .launchIn(scope)
    }

    private enum class UncommittedChangesType {
        None,
        Staged,
        Unstaged,
    }
}
