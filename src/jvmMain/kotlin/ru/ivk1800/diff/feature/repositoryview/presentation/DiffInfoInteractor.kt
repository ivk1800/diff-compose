package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffId
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DiffInfoInteractor(
    private val repoDirectory: File,
    private val diffRepository: DiffRepository,
    private val commitsRepository: CommitsRepository,
    private val diffInfoItemMapper: DiffInfoItemMapper,
) {
    // TODO: add main dispatcher
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val selectCommitEvent = MutableSharedFlow<Event>(extraBufferCapacity = 1)

    private val _state = MutableStateFlow<DiffInfoState>(DiffInfoState.None)
    val state: StateFlow<DiffInfoState>
        get() = _state

    init {
        selectCommitEvent
            .flatMapLatest { event ->
                flow {
                    when (event) {
                        is Event.DiffSelected -> handleDiffSelectedEvent(event)
                        is Event.FileSelected -> handleFileSelectedEvent(event)
                        Event.Unselected -> emit(DiffInfoState.None)
                    }
                }
                    .catch { error ->
                        emit(
                            DiffInfoState.Error(
                                message = "$error",
                            )
                        )
                    }
            }
            .onEach { state ->
                _state.value = state
            }.launchIn(scope)
    }

    fun onFileSelected(commitHash: String, path: String) {
        selectCommitEvent.tryEmit(Event.FileSelected(commitHash = commitHash, path = path))
    }

    fun onDiffSelected(diffId: DiffId) {
        selectCommitEvent.tryEmit(Event.DiffSelected(diffId))
    }

    fun onFileUnselected() {
        selectCommitEvent.tryEmit(Event.Unselected)
    }

    fun dispose() {
        scope.cancel()
    }

    private suspend fun FlowCollector<DiffInfoState>.handleFileSelectedEvent(event: Event.FileSelected) {
        val commit = requireNotNull(
            commitsRepository.getCommit(repoDirectory, event.commitHash)
        )

        val diff = diffRepository.getDiff(
            repoDirectory,
            oldCommitHash = requireNotNull(commit.parents.firstOrNull()),
            newCommitHash = event.commitHash,
            filePath = event.path,
        )

        emit(
            DiffInfoState.Content(
                items = diffInfoItemMapper.mapToItems(diff)
            )
        )
    }

    private suspend fun FlowCollector<DiffInfoState>.handleDiffSelectedEvent(event: Event.DiffSelected) {
        val diff = diffRepository.getDiff(
            repoDirectory,
            oldBlobId = event.diffId.oldId,
            newBlobId = event.diffId.newId,
        )
        emit(
            DiffInfoState.Content(
                items = diffInfoItemMapper.mapToItems(diff)
            )
        )
    }

    private sealed interface Event {
        data class FileSelected(val commitHash: String, val path: String) : Event
        data class DiffSelected(val diffId: DiffId) : Event
        object Unselected : Event
    }
}
