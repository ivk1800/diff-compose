package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.domain.UncommittedRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffId
import java.io.File
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class UncommittedChangesInteractor internal constructor(
    private val repoDirectory: File,
    private val diffRepository: DiffRepository,
    private val uncommittedRepository: UncommittedRepository,
    private val commitInfoMapper: CommitInfoMapper,
    context: CoroutineContext,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    private var rawStagedDiff: List<Diff> = emptyList()
    private var rawUnstagedDiff: List<Diff> = emptyList()
    private var rawUntrackedFiles: List<String> = emptyList()

    private val selectedFiles = MutableStateFlow<SelectedStatedFiles>(
        SelectedStatedFiles(persistentSetOf(), persistentSetOf())
    )

    private val checkEvent = MutableSharedFlow<Event>(replay = 1)
    private val errorsFlow = MutableSharedFlow<Throwable>(extraBufferCapacity = 1)

    private val _state = MutableStateFlow<UncommittedChangesState>(
        UncommittedChangesState.None,
    )
    val state: StateFlow<UncommittedChangesState>
        get() = _state

    val errors: Flow<Throwable>
        get() = errorsFlow

    constructor(
        repoDirectory: File,
        diffRepository: DiffRepository,
        commitInfoMapper: CommitInfoMapper,
        uncommittedRepository: UncommittedRepository
    ) : this(
        repoDirectory,
        diffRepository,
        uncommittedRepository,
        commitInfoMapper,
        Dispatchers.Main,
    )

    init {
        checkEvent
            .flatMapLatest { event ->
                when (event) {
                    Event.Check -> checkInternalFlow()

                    Event.AddAllToStaged -> flow {
                        _state.value = _state.value.tryCopyWithVcsProcess(
                            stagedVcsProcess = false,
                            unstagedVcsProcess = true,
                        )

                        uncommittedRepository.addAllToStaged(repoDirectory)
                        emit(Unit)
                    }
                        .flatMapLatest { checkInternalFlow() }

                    Event.RemoveAllFromStaged -> flow {
                        _state.value = _state.value.tryCopyWithVcsProcess(
                            stagedVcsProcess = true,
                            unstagedVcsProcess = false,
                        )

                        uncommittedRepository.removeAllFromStaged(repoDirectory)
                        emit(Unit)
                    }
                        .flatMapLatest { checkInternalFlow() }

                    is Event.AddFilesToStaged -> flow {
                        uncommittedRepository.addFilesToStaged(repoDirectory, event.ids.map { it.path })
                        emit(Unit)
                    }
                        .flatMapLatest { checkInternalFlow() }

                    is Event.RemoveFilesFromStaged -> flow {
                        uncommittedRepository.removeFilesFromStaged(repoDirectory, event.ids.map { it.path })
                        emit(Unit)
                    }
                        .flatMapLatest { checkInternalFlow() }
                }
                    .catch {
                        emit(UncommittedChangesState.None)
                        it.printStackTrace()
                        errorsFlow.emit(it)
                    }
            }
            .onEach { newState ->
                val stateValue = state.value
                if (stateValue is UncommittedChangesState.Content) {
                    _state.value = stateValue.copyWithVcsProcess(
                        stagedVcsProcess = false,
                        unstagedVcsProcess = false,
                    )
                }
                _state.value = newState
            }
            .launchIn(scope)
    }

    fun selectStatedFiles(files: ImmutableSet<CommitFileId>) {
        selectedFiles.value = SelectedStatedFiles(
            staged = files,
            unstaged = persistentSetOf(),
        )
    }

    fun selectUnstatedFiles(files: ImmutableSet<CommitFileId>) {
        selectedFiles.value = SelectedStatedFiles(
            staged = persistentSetOf(),
            unstaged = files,
        )
    }

    fun addAllToStaged() {
        scope.launch {
            checkEvent.emit(Event.AddAllToStaged)
        }
    }

    fun removeAllFromStaged() {
        scope.launch {
            checkEvent.emit(Event.RemoveAllFromStaged)
        }
    }

    fun addFilesToStaged(ids: Set<CommitFileId>) {
        scope.launch {
            checkEvent.emit(Event.AddFilesToStaged(ids))
        }
    }

    fun removeFilesFromStaged(ids: Set<CommitFileId>) {
        scope.launch {
            checkEvent.emit(Event.RemoveFilesFromStaged(ids))
        }
    }

    fun check() {
        scope.launch {
            checkEvent.emit(Event.Check)
        }
    }

    fun getDiffIdOfStagedOrNull(fileName: String): DiffId? = getDiffIdOrNull(rawStagedDiff, fileName)

    fun getDiffIdOfUnstagedOrNull(fileName: String): DiffId? = getDiffIdOrNull(rawUnstagedDiff, fileName)

    private fun getDiffIdOrNull(diffs: List<Diff>, fileName: String): DiffId? =
        diffs
            .find { diff -> diff.filePath == fileName }
            ?.let { diff ->
                DiffId(
                    oldId = diff.oldId,
                    newId = diff.newId,
                )
            }

    fun dispose() {
        scope.cancel()
    }

    private fun UncommittedChangesState.tryCopyWithVcsProcess(
        stagedVcsProcess: Boolean,
        unstagedVcsProcess: Boolean,
    ): UncommittedChangesState =
        if (this is UncommittedChangesState.Content) {
            copyWithVcsProcess(
                stagedVcsProcess = stagedVcsProcess,
                unstagedVcsProcess = unstagedVcsProcess,
            )
        } else {
            this
        }

    private fun UncommittedChangesState.Content.copyWithVcsProcess(
        stagedVcsProcess: Boolean,
        unstagedVcsProcess: Boolean,
    ): UncommittedChangesState.Content =
        copy(
            unstaged = unstaged.copy(
                vcsProcess = unstagedVcsProcess,
            ),
            staged = staged.copy(
                vcsProcess = stagedVcsProcess,
            )
        )

    private suspend fun checkInternalFlow(): Flow<UncommittedChangesState> =
        flow {
            val stagedDiff: List<Diff> = diffRepository.getStagedDiff(repoDirectory)
            val unstagedDiff: List<Diff> = diffRepository.getUnstagedDiff(repoDirectory)
            val untrackedFiles: List<String> = uncommittedRepository.getUntrackedFiles(repoDirectory)

            emit(RawData(stagedDiff, unstagedDiff, untrackedFiles))
        }
            .onEach { (stagedDiff, unstagedDiff, untrackedFiles) ->
                rawStagedDiff = stagedDiff
                rawUnstagedDiff = unstagedDiff
                rawUntrackedFiles = untrackedFiles
            }
            .flatMapLatest { (stagedDiff, unstagedDiff, rawUntrackedFiles) ->
                if (stagedDiff.isEmpty() && unstagedDiff.isEmpty() && rawUntrackedFiles.isEmpty()) {
                    flowOf(UncommittedChangesState.None)
                } else {
                    selectedFiles.map { selected ->
                        UncommittedChangesState.Content(
                            staged = UncommittedChangesState.Content.Staged(
                                selected = selected.staged,
                                vcsProcess = false,
                                files = commitInfoMapper.mapDiffToFiles(stagedDiff),
                            ),
                            unstaged = UncommittedChangesState.Content.Unstaged(
                                selected = selected.unstaged,
                                vcsProcess = false,
                                files = (commitInfoMapper.mapDiffToFiles(unstagedDiff) + rawUntrackedFiles.map {
                                    CommitFileItem(
                                        id = CommitFileId(path = it),
                                        name = it,
                                        type = CommitFileItem.Type.Added,
                                    )
                                }).toImmutableList(),
                            ),
                        )
                    }
                }
            }

    private sealed interface Event {
        object Check : Event

        object AddAllToStaged : Event

        object RemoveAllFromStaged : Event

        data class AddFilesToStaged(val ids: Set<CommitFileId>) : Event

        data class RemoveFilesFromStaged(val ids: Set<CommitFileId>) : Event
    }

    private data class SelectedStatedFiles(
        val staged: ImmutableSet<CommitFileId>,
        val unstaged: ImmutableSet<CommitFileId>,
    )

    private data class RawData(
        val stagedDiff: List<Diff>,
        val unstagedDiff: List<Diff>,
        val untrackedFiles: List<String>,
    )
}
