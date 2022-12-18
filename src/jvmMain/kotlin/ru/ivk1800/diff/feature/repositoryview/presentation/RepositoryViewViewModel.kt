package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.arch.presentation.BaseViewModel
import java.io.File

class RepositoryViewViewModel(
    private val repositoryDirectory: File,
    private val commitsInteractor: CommitsInteractor,
    private val commitInfoInteractor: CommitInfoInteractor,
    private val router: RepositoryViewRouter,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        RepositoryViewState(
            commitsTableState = CommitsTableState.Loading,
            commitInfoState = CommitInfoState.None,
        )
    )
    val state: StateFlow<RepositoryViewState>
        get() = _state

    init {
        commitInfoInteractor.state
            .map { newState ->
                _state.value.copy(commitInfoState = newState)
            }.onEach { newState ->
                _state.value = newState
            }
            .launchIn(viewModelScope)

        commitsInteractor.state
            .map { newState ->
                _state.value.copy(commitsTableState = newState)
            }
            .onEach { newState -> _state.value = newState }
            .launchIn(viewModelScope)
    }

    fun onEvent(value: RepositoryViewEvent) {
        when (value) {
            RepositoryViewEvent.OnReload -> commitsInteractor.reload()
            RepositoryViewEvent.OpenTerminal -> router.toTerminal(repositoryDirectory)
            RepositoryViewEvent.OpenFinder -> router.toFinder(repositoryDirectory)
            is RepositoryViewEvent.OnCommitsSelected -> {
                commitInfoInteractor.onCommitSelected(
                    if (value.range.first == value.range.last) {
                        commitsInteractor.getCommitHashByIndex(value.range.first)
                    } else {
                        null
                    }
                )
            }
            RepositoryViewEvent.OnCommitsUnselected -> commitInfoInteractor.onCommitSelected(null)
        }
    }


    override fun dispose() {
        commitsInteractor.dispose()
        commitInfoInteractor.dispose()
        super.dispose()
    }
}
