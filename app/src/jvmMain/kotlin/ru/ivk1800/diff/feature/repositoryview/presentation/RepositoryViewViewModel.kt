package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.event.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.event.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.event.SidePanelEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.UncommittedChangesManager
import ru.ivk1800.diff.feature.repositoryview.presentation.state.RepositoryViewState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.composer.RepositoryViewStateComposer
import ru.ivk1800.diff.presentation.BaseViewModel

class RepositoryViewViewModel(
    private val eventHandler: RepositoryViewEventHandler,
    repositoryViewStateComposer: RepositoryViewStateComposer,
    uncommittedChangesManager: UncommittedChangesManager,
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
        uncommittedChangesManager.check()
    }

    fun onEvent(value: RepositoryViewEvent) = eventHandler.onEvent(value)

    fun onHistoryEvent(value: HistoryEvent) = eventHandler.onHistoryEvent(value)

    fun onSidePanelEvent(value: SidePanelEvent) = eventHandler.onSidePanelEvent(value)
}
