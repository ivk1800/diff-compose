package ru.ivk1800.diff.feature.repositoryview.presentation.manager

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.mapper.DiffInfoItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.DiffInfoState
import ru.ivk1800.diff.presentation.ErrorTransformer
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class DiffInfoManager internal constructor(
    private val diffRepository: DiffRepository,
    private val commitsRepository: CommitsRepository,
    private val diffInfoItemMapper: DiffInfoItemMapper,
    private val errorTransformer: ErrorTransformer,
    context: CoroutineContext,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    private val eventsFlow = MutableSharedFlow<Event>()
    private val refreshEventFlow = MutableSharedFlow<Unit>()
    private val selectedLines = MutableStateFlow<ImmutableSet<DiffInfoItem.Id.Line>>(persistentSetOf())

    private var rawDiff: Diff? = null
    private val _state = MutableStateFlow<DiffInfoState>(DiffInfoState.None)
    val state: StateFlow<DiffInfoState>
        get() = _state

    constructor(
        diffRepository: DiffRepository,
        commitsRepository: CommitsRepository,
        diffInfoItemMapper: DiffInfoItemMapper,
        errorTransformer: ErrorTransformer,
    ) : this(
        diffRepository,
        commitsRepository,
        diffInfoItemMapper,
        errorTransformer,
        Dispatchers.Main,
    )

    init {
        eventsFlow
            .onEach {
                rawDiff = null
            }
            .flatMapLatest { event ->
                when (event) {
                    is Event.DiffSelected -> refreshEventFlow
                        .onStart { emit(Unit) }
                        .flatMapLatest { handleDiffSelectedEvent(event) }

                    is Event.FileSelected -> refreshEventFlow
                        .onStart { emit(Unit) }
                        .flatMapLatest { handleFileSelectedEvent(event) }

                    Event.Unselected -> flowOf(DiffInfoState.None)
                }
                    .catch { error ->
                        emit(
                            DiffInfoState.Error(
                                message = errorTransformer.transformForDisplay(error)
                            )
                        )
                    }
            }
            .onEach { state ->
                _state.value = state
            }.launchIn(scope)
    }

    fun onFileSelected(commitHash: String, path: String) {
        scope.launch { eventsFlow.emit(Event.FileSelected(commitHash = commitHash, path = path)) }
    }

    fun selectUncommittedFiles(fileName: String, type: UncommittedChangesType) {
        scope.launch { eventsFlow.emit(Event.DiffSelected(fileName, type)) }
    }

    fun refresh() {
        scope.launch { refreshEventFlow.emit(Unit) }
    }

    fun unselect() {
        scope.launch { eventsFlow.emit(Event.Unselected) }
    }

    fun selectLines(lines: ImmutableSet<DiffInfoItem.Id.Line>) {
        selectedLines.value = lines
    }

    fun getDiffId(): String? = rawDiff?.newId

    fun getHunk(id: DiffInfoItem.Id.Hunk): Diff.Hunk? {
        return rawDiff?.hunks?.get(id.number - 1)
    }

    fun dispose() {
        scope.cancel()
    }

    private fun handleFileSelectedEvent(event: Event.FileSelected): Flow<DiffInfoState> =
        combine(
            flow {
                val commit = requireNotNull(
                    commitsRepository.getCommit(event.commitHash)
                )

                val diff = diffRepository.getDiff(
                    oldCommitHash = requireNotNull(commit.parents.firstOrNull()),
                    newCommitHash = event.commitHash,
                    filePath = event.path,
                )

                emit(diff)
            },
            selectedLines,
        ) { diff, selected ->
            rawDiff = diff
            DiffInfoState.Content(
                selected = selected,
                // TODO: Improve remapping items
                items = diffInfoItemMapper.mapToItems(DiffInfoItemMapper.DiffType.Commit, diff, selected),
            )
        }

    private fun handleDiffSelectedEvent(event: Event.DiffSelected): Flow<DiffInfoState> =
        combine(
            flow {
                val diff = when (event.type) {
                    UncommittedChangesType.Staged -> diffRepository.getStagedFileDiff(event.fileName)
                    UncommittedChangesType.Unstaged -> diffRepository.getUnstagedFileDiff(event.fileName)
                }
                emit(diff)
            },
            selectedLines,
        ) { diff, selected ->
            rawDiff = diff
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
            val fileName: String,
            val type: UncommittedChangesType,
        ) : Event

        object Unselected : Event
    }
}
