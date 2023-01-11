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
        listenCommitInfo()
        listenSelectedCommits()
        listenSelectedCommitFiles()
        listenUncommittedChanges()
    }

    fun selectCommitFiles(items: ImmutableSet<CommitFileId>) {
        commitInfoInteractor.selectFiles(items)
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
    }

    fun selectUnstatedFiles(files: ImmutableSet<CommitFileId>) {
        uncommittedChangesInteractor.selectUnstatedFiles(files)
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

    private fun listenSelectedCommitFiles() {
        commitInfoInteractor.state.map {
            when (it) {
                is CommitInfoState.Content -> it.selected
                is CommitInfoState.Error,
                CommitInfoState.None -> persistentSetOf()
            }
        }.onEach { selected ->
            if (selected.size == 1) {
                diffInfoInteractor.onFileSelected(
                    commitHash = requireNotNull(commitInfoInteractor.selectedCommitHash),
                    path = selected.first().path,
                )
            } else {
                diffInfoInteractor.unselect()
            }
        }.launchIn(scope)
    }

    private fun listenCommitInfo() {
        commitInfoInteractor.state
            .onEach {
                diffInfoInteractor.selectLines(persistentSetOf())
            }
            .launchIn(scope)
    }

    private fun listenUncommittedChanges() {
        uncommittedChangesInteractor.state.map { state ->
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
                    UncommittedChangesType.None -> diffInfoInteractor.unselect()
                    UncommittedChangesType.Staged -> {
                        if (selected.size == 1) {
                            val selectedFile = selected.first()
                            diffInfoInteractor.selectUncommittedFiles(
                                selectedFile.path,
                                DiffInfoInteractor.UncommittedChangesType.Staged,
                            )
                        } else {
                            diffInfoInteractor.unselect()
                        }
                    }

                    UncommittedChangesType.Unstaged -> {
                        if (selected.size == 1) {
                            val selectedFile = selected.first()
                            diffInfoInteractor.selectUncommittedFiles(
                                selectedFile.path,
                                DiffInfoInteractor.UncommittedChangesType.Unstaged,
                            )
                        } else {
                            diffInfoInteractor.unselect()
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
