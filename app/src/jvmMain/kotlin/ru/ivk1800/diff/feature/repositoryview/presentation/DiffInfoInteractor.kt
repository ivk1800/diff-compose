package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
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
    private val selectedLines = MutableStateFlow<ImmutableSet<DiffInfoItem.Id.Line>>(persistentSetOf())

    private val _state = MutableStateFlow<DiffInfoState>(DiffInfoState.None)
    val state: StateFlow<DiffInfoState>
        get() = _state

    init {
        selectCommitEvent
            .flatMapLatest { event ->
                when (event) {
                    is Event.DiffSelected -> handleDiffSelectedEvent(event)
                    is Event.FileSelected -> handleFileSelectedEvent(event)
                    Event.Unselected -> flowOf(DiffInfoState.None)
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

    fun onDiffSelected(diffId: DiffId, type: UncommittedChangesType) {
        selectCommitEvent.tryEmit(Event.DiffSelected(diffId, type))
    }

    fun onFileUnselected() {
        selectCommitEvent.tryEmit(Event.Unselected)
    }

    fun selectLines(lines: ImmutableSet<DiffInfoItem.Id.Line>) {
        selectedLines.value = lines
    }

    fun dispose() {
        scope.cancel()
    }

    private fun handleFileSelectedEvent(event: Event.FileSelected): Flow<DiffInfoState> =
        combine(
            flow {
                val commit = requireNotNull(
                    commitsRepository.getCommit(repoDirectory, event.commitHash)
                )

                val diff = diffRepository.getDiff(
                    repoDirectory,
                    oldCommitHash = requireNotNull(commit.parents.firstOrNull()),
                    newCommitHash = event.commitHash,
                    filePath = event.path,
                )

                emit(diff)
            },
            selectedLines,
        ) { diff, selected ->
            DiffInfoState.Content(
                selected = selected,
                // TODO: Improve remapping items
                items = diffInfoItemMapper.mapToItems(DiffInfoItemMapper.DiffType.Commit, diff, selected),
            )
        }

    private fun handleDiffSelectedEvent(event: Event.DiffSelected): Flow<DiffInfoState> =
        combine(
            flow {
                emit(
                    diffRepository.getDiff(
                        repoDirectory,
                        oldBlobId = event.diffId.oldId,
                        newBlobId = event.diffId.newId,
                    )
                )
            },
            selectedLines,
        ) { diff, selected ->
            DiffInfoState.Content(
                selected = selected,
                // TODO: Improve remapping items
                items = diffInfoItemMapper.mapToItems(
                    type = when (event.type) {
                        UncommittedChangesType.Staged -> DiffInfoItemMapper.DiffType.UncommittedChanges.Staged
                        UncommittedChangesType.Unstaged -> DiffInfoItemMapper.DiffType.UncommittedChanges.Unstaged
                    },
                    diff,
                    selected,
                ),
            )
        }

    enum class UncommittedChangesType {
        Staged,
        Unstaged,
    }

    private sealed interface Event {
        data class FileSelected(val commitHash: String, val path: String) : Event
        data class DiffSelected(
            val diffId: DiffId,
            val type: UncommittedChangesType,
        ) : Event
        object Unselected : Event
    }
}
