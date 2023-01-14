package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.presentation.BaseViewModel
import ru.ivk1800.diff.presentation.DialogRouter
import ru.ivk1800.diff.presentation.ErrorTransformer

class RepositoryViewViewModel(
    private val diffInfoInteractor: DiffInfoInteractor,
    private val commitsTableInteractor: CommitsTableInteractor,
    private val dialogRouter: DialogRouter,
    private val filesInfoInteractor: FilesInfoInteractor,
    private val errorTransformer: ErrorTransformer,
    private val repositoryViewEventHandler: RepositoryViewEventHandler,
    uncommittedChangesInteractor: UncommittedChangesInteractor,
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
                    DialogRouter.Dialog.Error(
                        title = "Error",
                        text = errorTransformer.transformForDisplay(error),
                    ),
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(value: RepositoryViewEvent) { repositoryViewEventHandler.onEvent(value) }

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
