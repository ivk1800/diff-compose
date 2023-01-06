package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.presentation.BaseViewModel
import ru.ivk1800.diff.presentation.DialogRouter
import java.io.File

class RepositoryViewViewModel(
    private val repositoryDirectory: File,
    private val commitInfoInteractor: CommitInfoInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
    private val uncommittedChangesInteractor: UncommittedChangesInteractor,
    private val selectionCoordinator: SelectionCoordinator,
    private val commitsTableInteractor: CommitsTableInteractor,
    private val router: RepositoryViewRouter,
    private val dialogRouter: DialogRouter,
    private val filesInfoInteractor: FilesInfoInteractor,
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
                fun getMessage(e: Throwable): String {
                    val cause = e.cause
                    return if (cause == null) {
                        e.message.orEmpty()
                    } else {
                        listOf(e.message.orEmpty(), getMessage(cause)).joinToString("\n")
                    }
                }

                dialogRouter.show(
                    DialogRouter.Dialog(
                        title = "Error",
                        text = getMessage(error),
                    ),
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(value: RepositoryViewEvent) {
        when (value) {
            RepositoryViewEvent.OnReload -> {
                commitInfoInteractor.selectCommit(null)
                diffInfoInteractor.onFileUnselected()
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
                if (value.items.size == 1) {
                    diffInfoInteractor.onFileSelected(
                        commitHash = requireNotNull(commitInfoInteractor.selectedCommitHash),
                        path = value.items.first().path,
                    )
                } else {
                    diffInfoInteractor.onFileUnselected()
                }

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
                when(value) {
                    is RepositoryViewEvent.Diff.OnLinesSelected ->
                        selectionCoordinator.selectDiffLines(value.ids)
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
}
