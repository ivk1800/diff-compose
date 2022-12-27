package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.presentation.BaseViewModel
import java.io.File

class RepositoryViewViewModel(
    private val repositoryDirectory: File,
    private val commitsInteractor: CommitsInteractor,
    private val commitInfoInteractor: CommitInfoInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
    private val uncommittedChangesInteractor: UncommittedChangesInteractor,
    private val tableCommitsStateTransformer: TableCommitsStateTransformer,
    private val router: RepositoryViewRouter,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        RepositoryViewState(
            commitsTableState = CommitsTableState.Loading,
            diffInfoState = DiffInfoState.None,
            activeState = ActiveState.None,
        )
    )
    val state: StateFlow<RepositoryViewState>
        get() = _state

    init {
        uncommittedChangesInteractor.check()
        commitInfoInteractor
            .state
            .combine(uncommittedChangesInteractor.state) { commitInfo, uncommittedChanges ->
                _state.value.copy(
                    activeState = if (commitInfo is CommitInfoState.Content) {
                        ActiveState.Commit(commitInfo)
                    } else if (uncommittedChanges is UncommittedChangesState.Content) {
                        ActiveState.UncommittedChanges(uncommittedChanges)
                    } else {
                        ActiveState.None
                    },
                )
            }
            .onEach { newState -> _state.value = newState }
            .launchIn(viewModelScope)

        tableCommitsStateTransformer.transform(
            scope = viewModelScope,
            commitsTableStateFlow = commitsInteractor.state,
            uncommittedChangesStateFlow = uncommittedChangesInteractor.state,
        )
            .map { newState ->
                _state.value.copy(commitsTableState = newState)
            }
            .onEach { newState -> _state.value = newState }
            .launchIn(viewModelScope)

        diffInfoInteractor.state
            .map { newState ->
                _state.value.copy(diffInfoState = newState)
            }
            .onEach { newState -> _state.value = newState }
            .launchIn(viewModelScope)
    }

    fun onEvent(value: RepositoryViewEvent) {
        when (value) {
            RepositoryViewEvent.OnReload -> {
                commitInfoInteractor.onCommitSelected(null)
                diffInfoInteractor.onFileUnselected()
                commitsInteractor.reload()
                uncommittedChangesInteractor.check()
            }

            RepositoryViewEvent.OpenTerminal -> router.toTerminal(repositoryDirectory)
            RepositoryViewEvent.OpenFinder -> router.toFinder(repositoryDirectory)
            is RepositoryViewEvent.OnCommitsSelected -> {
                commitInfoInteractor.onCommitSelected(
                    if (value.items.size == 1) {
                        value.items.first().hash
                    } else {
                        null
                    }
                )
            }

            RepositoryViewEvent.OnCommitsUnselected -> commitInfoInteractor.onCommitSelected(null)
            is RepositoryViewEvent.OnFilesSelected ->
                if (value.range.first == value.range.last) {
                    diffInfoInteractor.onFileSelected(
                        commitHash = requireNotNull(commitInfoInteractor.selectedCommitHash),
                        path = requireNotNull(
                            commitInfoInteractor.getFilePathByIndex(value.range.first)
                        ),
                    )
                } else {
                    diffInfoInteractor.onFileUnselected()
                }

            RepositoryViewEvent.OnFilesUnselected -> diffInfoInteractor.onFileUnselected()
            RepositoryViewEvent.OnLoadMoreCommits -> commitsInteractor.loadMore()
            RepositoryViewEvent.OnUncommittedChangesSelected -> {
                commitInfoInteractor.onCommitSelected(null)
            }

            is RepositoryViewEvent.UncommittedChanges ->
                when (value) {
                    RepositoryViewEvent.UncommittedChanges.OnAddAllToStaged ->
                        uncommittedChangesInteractor.addAllToStaged()

                    RepositoryViewEvent.UncommittedChanges.OnRemoveAllFromStaged ->
                        uncommittedChangesInteractor.removeAllFromStaged()
                }
        }
    }

    override fun dispose() {
        commitsInteractor.dispose()
        uncommittedChangesInteractor.dispose()
        commitInfoInteractor.dispose()
        diffInfoInteractor.dispose()
        super.dispose()
    }
}
