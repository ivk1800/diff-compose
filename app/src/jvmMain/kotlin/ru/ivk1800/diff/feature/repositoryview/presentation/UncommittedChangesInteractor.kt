package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableList
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
import ru.ivk1800.diff.ext.onFirst
import ru.ivk1800.diff.feature.repositoryview.domain.ChangeType
import ru.ivk1800.diff.feature.repositoryview.domain.CommitFile
import ru.ivk1800.diff.feature.repositoryview.domain.StatusRepository
import ru.ivk1800.diff.feature.repositoryview.domain.UncommittedRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.helper.UncommittedChangesNextSelectionHelper
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import java.io.File
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class UncommittedChangesInteractor internal constructor(
    private val repoDirectory: File,
    private val statusRepository: StatusRepository,
    private val uncommittedRepository: UncommittedRepository,
    private val selectionHelper: UncommittedChangesNextSelectionHelper,
    context: CoroutineContext,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    private var rawStagedFiles: List<CommitFile> = emptyList()
    private var rawUntrackedFiles: List<CommitFile> = emptyList()
    private var rawAllUnstagedFiles: List<CommitFile> = emptyList()

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
        statusRepository: StatusRepository,
        uncommittedRepository: UncommittedRepository
    ) : this(
        repoDirectory,
        statusRepository,
        uncommittedRepository,
        UncommittedChangesNextSelectionHelper(),
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

                    is Event.AddFilesToStaged -> handleAddFilesToStagedFlow(event)

                    is Event.RemoveFilesFromStaged -> handleRemoveFilesFromStagedFlow(event)
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
            emit(statusRepository.getStatus(repoDirectory))
        }
            .onEach { (staged, unstaged, untracked) ->
                rawStagedFiles = staged
                rawAllUnstagedFiles = unstaged + untracked
                rawUntrackedFiles = untracked
            }
            .flatMapLatest { (staged, unstaged, untracked) ->
                if (staged.isEmpty() && unstaged.isEmpty() && untracked.isEmpty()) {
                    flowOf(UncommittedChangesState.None)
                } else {
                    selectedFiles.map { selected ->
                        UncommittedChangesState.Content(
                            staged = UncommittedChangesState.Content.Staged(
                                selected = selected.staged,
                                vcsProcess = false,
                                files = toFileItems(staged),
                            ),
                            unstaged = UncommittedChangesState.Content.Unstaged(
                                selected = selected.unstaged,
                                vcsProcess = false,
                                files = (toFileItems(unstaged) + toFileItems(untracked)).toImmutableList(),
                            ),
                        )
                    }
                }
            }

    private fun handleAddFilesToStagedFlow(event: Event.AddFilesToStaged) =
        flow {
            uncommittedRepository.addFilesToStaged(repoDirectory, event.ids.map { it.path })
            if (event.ids.size == 1) {
                emit(
                    selectionHelper.calculateIndex(
                        rawAllUnstagedFiles,
                        event.ids.first(),
                    )
                )
            } else {
                emit(null)
            }
        }
            .flatMapLatest { indexForSelection ->
                checkInternalFlow()
                    .onFirst {
                        if (indexForSelection != null) {
                            selectUnstatedFiles(
                                selectionHelper.confirm(rawAllUnstagedFiles, indexForSelection),
                            )
                        } else {
                            selectStatedFiles(persistentSetOf())
                        }
                    }
            }

    private fun handleRemoveFilesFromStagedFlow(event: Event.RemoveFilesFromStaged) =
        flow {
            uncommittedRepository.removeFilesFromStaged(repoDirectory, event.ids.map { it.path })
            if (event.ids.size == 1) {
                emit(
                    selectionHelper.calculateIndex(
                        rawStagedFiles,
                        event.ids.first(),
                    )
                )
            } else {
                emit(null)
            }
        }
            .flatMapLatest { indexForSelection ->
                checkInternalFlow()
                    .onFirst {
                        if (indexForSelection != null) {
                            selectStatedFiles(
                                selectionHelper.confirm(rawStagedFiles, indexForSelection),
                            )
                        } else {
                            selectStatedFiles(persistentSetOf())
                        }
                    }
            }

    private fun toFileItems(files: List<CommitFile>): ImmutableList<CommitFileItem> =
        files.map { file ->
            CommitFileItem(
                id = CommitFileId(path = file.name),
                name = file.name,
                type = when (file.changeType) {
                    ChangeType.Add -> CommitFileItem.Type.Added
                    ChangeType.Modify -> CommitFileItem.Type.Modified
                    ChangeType.Delete -> CommitFileItem.Type.Deleted
                    ChangeType.Rename -> CommitFileItem.Type.Renamed
                    ChangeType.Copy -> CommitFileItem.Type.Copied
                }
            )
        }.toImmutableList()

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
}
