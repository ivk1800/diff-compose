package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem

@OptIn(ExperimentalCoroutinesApi::class)
class DiffInfoInteractor {
    // TODO: add main dispatcher
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val selectCommitEvent = MutableSharedFlow<Unit?>(extraBufferCapacity = 1)

    private val _state = MutableStateFlow<DiffInfoState>(DiffInfoState.None)
    val state: StateFlow<DiffInfoState>
        get() = _state

    init {
        selectCommitEvent
            .flatMapLatest { hash ->
                flow {
                    if (hash != null) {
                        emit(
                            DiffInfoState.Content(
                                items = persistentListOf(
                                    DiffInfoItem.HunkHeader(
                                        text = "Hunk: Lines 19-26",
                                    ),
                                    DiffInfoItem.Line(
                                        text = "test1",
                                        type = DiffInfoItem.Line.Type.None,
                                    ),
                                    DiffInfoItem.Line(
                                        text = "test2",
                                        type = DiffInfoItem.Line.Type.Added,
                                    ),
                                    DiffInfoItem.Line(
                                        text = "test3",
                                        type = DiffInfoItem.Line.Type.Removed,
                                    ),
                                )
                            )
                        )
                    } else {
                        emit(DiffInfoState.None)
                    }

                }
            }
            .onEach { state ->
                _state.value = state
            }.launchIn(scope)
    }

    fun onFileSelected() {
        selectCommitEvent.tryEmit(Unit)
    }

    fun onFileUnselected() {
        selectCommitEvent.tryEmit(null)
    }

    fun dispose() {
        scope.cancel()
    }
}
