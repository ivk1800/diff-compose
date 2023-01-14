package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitsTableState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.DiffInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FilesInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.HistoryState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.HistoryStateComposer
import ru.ivk1800.diff.presentation.BaseViewModel
import ru.ivk1800.diff.presentation.DialogRouter
import ru.ivk1800.diff.presentation.ErrorTransformer

class RepositoryViewViewModel(
    private val dialogRouter: DialogRouter,
    private val errorTransformer: ErrorTransformer,
    private val repositoryViewEventHandler: RepositoryViewEventHandler,
    historyStateComposer: HistoryStateComposer,
    uncommittedChangesInteractor: UncommittedChangesInteractor,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        HistoryState(
            commitsTableState = CommitsTableState.Loading,
            diffInfoState = DiffInfoState.None,
            filesInfoState = FilesInfoState.None,
        )
    )
    val state: StateFlow<HistoryState>
        get() = _state

    init {
        uncommittedChangesInteractor.check()
        historyStateComposer.getState(viewModelScope)
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
}
