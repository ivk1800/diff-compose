package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.event.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.event.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.event.SidePanelEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.state.RepositoryViewState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.composer.RepositoryViewStateComposer
import ru.ivk1800.diff.presentation.BaseViewModel
import ru.ivk1800.diff.presentation.DialogRouter
import ru.ivk1800.diff.presentation.ErrorTransformer

class RepositoryViewViewModel(
    private val dialogRouter: DialogRouter,
    private val errorTransformer: ErrorTransformer,
    private val repositoryViewEventHandler: RepositoryViewEventHandler,
    repositoryViewStateComposer: RepositoryViewStateComposer,
    uncommittedChangesInteractor: UncommittedChangesInteractor,
) : BaseViewModel() {

    private val _state = repositoryViewStateComposer
        .getState(viewModelScope)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repositoryViewStateComposer.getDefaultState(),
        )

    val state: StateFlow<RepositoryViewState>
        get() = _state

    init {
        uncommittedChangesInteractor.check()
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

    fun onEvent(value: RepositoryViewEvent) = repositoryViewEventHandler.onEvent(value)

    fun onHistoryEvent(value: HistoryEvent) = repositoryViewEventHandler.onHistoryEvent(value)

    fun onSidePanelEvent(value: SidePanelEvent) = repositoryViewEventHandler.onSidePanelEvent(value)
}
