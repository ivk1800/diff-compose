package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.ivk1800.diff.presentation.BaseViewModel
import ru.ivk1800.diff.presentation.DialogRouter
import ru.ivk1800.diff.presentation.ErrorTransformer
import java.io.File

class RepositoryViewViewModel(
    private val repositoryDirectory: File,
    private val commitInfoInteractor: CommitInfoInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
    private val uncommittedChangesInteractor: UncommittedChangesInteractor,
    private val selectionCoordinator: SelectionCoordinator,
    private val commitsTableInteractor: CommitsTableInteractor,
    private val diffOperationsInteractor: DiffOperationsInteractor,
    private val router: RepositoryViewRouter,
    private val dialogRouter: DialogRouter,
    private val filesInfoInteractor: FilesInfoInteractor,
    private val errorTransformer: ErrorTransformer,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        RepositoryViewState(
            commitsTableState = CommitsTableState.Loading,
            diffInfoState = DiffInfoState.None,
            filesInfoState = FilesInfoState.None,
        )
    )
    val state: StateFlow<RepositoryViewState>
        get() = _state

    init {
        uncommittedChangesInteractor.check()
        getStateFlow()
            .onEach { newState -> _state.value = newState }
            .launchIn(viewModelScope)

        uncommittedChangesInteractor.errors
            .onEach { error ->
                dialogRouter.show(
                    DialogRouter.Dialog(
                        title = "Error",
                        text = errorTransformer.transformForDisplay(error),
                    ),
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(value: RepositoryViewEvent) {
        when (value) {
            RepositoryViewEvent.OnReload -> {
                commitInfoInteractor.selectCommit(null)
                diffInfoInteractor.unselect()
                commitsTableInteractor.reload()
                uncommittedChangesInteractor.check()
            }

            RepositoryViewEvent.OpenTerminal -> router.toTerminal(repositoryDirectory)
            RepositoryViewEvent.OpenFinder -> router.toFinder(repositoryDirectory)
            is RepositoryViewEvent.OnCommitsSelected -> {
                selectionCoordinator.selectCommits(value.items)
            }

            RepositoryViewEvent.OnCommitsUnselected -> commitInfoInteractor.selectCommit(null)
            is RepositoryViewEvent.OnFilesSelected ->
                selectionCoordinator.selectCommitFiles(value.items)

            RepositoryViewEvent.OnLoadMoreCommits -> commitsTableInteractor.loadMore()
            RepositoryViewEvent.OnUncommittedChangesSelected -> selectionCoordinator.selectUncommittedChanges()

            is RepositoryViewEvent.UncommittedChanges ->
                when (value) {
                    RepositoryViewEvent.UncommittedChanges.OnAddAllToStaged ->
                        uncommittedChangesInteractor.addAllToStaged()

                    RepositoryViewEvent.UncommittedChanges.OnRemoveAllFromStaged ->
                        uncommittedChangesInteractor.removeAllFromStaged()

                    is RepositoryViewEvent.UncommittedChanges.OnAddFilesToStaged ->
                        uncommittedChangesInteractor.addFilesToStaged(value.ids)

                    is RepositoryViewEvent.UncommittedChanges.OnRemoveFilesFromStaged ->
                        uncommittedChangesInteractor.removeFilesFromStaged(value.ids)

                    is RepositoryViewEvent.UncommittedChanges.OnStatedFilesSelected ->
                        selectionCoordinator.selectStatedFiles(value.items)

                    is RepositoryViewEvent.UncommittedChanges.OnUnstatedFilesSelected ->
                        selectionCoordinator.selectUnstatedFiles(value.items)
                }

            is RepositoryViewEvent.Diff -> {
                when (value) {
                    is RepositoryViewEvent.Diff.OnLinesSelected ->
                        selectionCoordinator.selectDiffLines(value.ids)

                    is RepositoryViewEvent.Diff.OnUnstageHunk -> onUnstageHunk(value)
                    is RepositoryViewEvent.Diff.OnDiscardHunk -> onDiscardHunk(value)
                }
            }
        }
    }

    private fun getStateFlow(): Flow<RepositoryViewState> =
        combine(
            filesInfoInteractor.state,
            commitsTableInteractor.state,
            diffInfoInteractor.state,
        ) { activeState, commitsTableState, diffInfoState ->
            RepositoryViewState(
                commitsTableState = commitsTableState,
                diffInfoState = diffInfoState,
                filesInfoState = activeState,
            )
        }

    private fun onUnstageHunk(event: RepositoryViewEvent.Diff.OnUnstageHunk) {
        viewModelScope.launch {
            val result = diffOperationsInteractor.unstageHunk(event.hunkId)
            val error = result.exceptionOrNull()
            if (error != null) {
                dialogRouter.show(
                    DialogRouter.Dialog(
                        title = "Error",
                        text = errorTransformer.transformForDisplay(error),
                    )
                )
            } else {
                uncommittedChangesInteractor.check()
                diffInfoInteractor.refresh()
            }
        }
    }

    private fun onDiscardHunk(event: RepositoryViewEvent.Diff.OnDiscardHunk) {
        viewModelScope.launch {
            val result = diffOperationsInteractor.discardHunk(event.hunkId)
            val error = result.exceptionOrNull()
            if (error != null) {
                dialogRouter.show(
                    DialogRouter.Dialog(
                        title = "Error",
                        text = errorTransformer.transformForDisplay(error),
                    )
                )
            } else {
                uncommittedChangesInteractor.check()
                diffInfoInteractor.refresh()
            }
        }
    }

}
