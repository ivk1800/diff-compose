package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.CoroutineScope
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class UncommittedChangesInteractor(
    private val repoDirectory: File,
    private val diffRepository: DiffRepository,
    private val commitInfoMapper: CommitInfoMapper,
) {
    // TODO: add main dispatcher
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val checkEvent = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    private val errorsFlow = MutableSharedFlow<Throwable>(extraBufferCapacity = 1)

    private val _state = MutableStateFlow<UncommittedChangesState>(
        UncommittedChangesState.None,
    )
    val state: StateFlow<UncommittedChangesState>
        get() = _state

    val errors: Flow<Throwable>
        get() = errorsFlow

    init {
        checkEvent
            .flatMapLatest { event ->
                when (event) {
                    Event.Check -> flow {
                        emit(checkInternal())
                    }

                    Event.AddAllToStaged -> flow {
                        _state.value = _state.value.tryCopyWithVcsProcess(
                            stagedVcsProcess = false,
                            unstagedVcsProcess = true,
                        )

                        diffRepository.addAllToStaged(repoDirectory)
                        emit(checkInternal())
                    }

                    Event.RemoveAllFromStaged -> flow {
                        _state.value = _state.value.tryCopyWithVcsProcess(
                            stagedVcsProcess = true,
                            unstagedVcsProcess = false,
                        )

                        diffRepository.removeAllFromStaged(repoDirectory)
                        emit(checkInternal())
                    }

                    is Event.AddFilesToStaged -> flow {
                        diffRepository.addFilesToStaged(repoDirectory, event.ids.map { it.path })
                        emit(checkInternal())
                    }

                    is Event.RemoveFilesFromStaged -> flow {
                        diffRepository.removeFilesFromStaged(repoDirectory, event.ids.map { it.path })
                        emit(checkInternal())
                    }
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

    private suspend fun checkInternal(): UncommittedChangesState {
        val stagedDiff: List<Diff> = diffRepository.getStagedDiff(repoDirectory)
        val unstagedDiff: List<Diff> = diffRepository.getUnstagedDiff(repoDirectory)

        return if (stagedDiff.isEmpty() && unstagedDiff.isEmpty()) {
            UncommittedChangesState.None
        } else {
            UncommittedChangesState.Content(
                staged = UncommittedChangesState.Content.Staged(
                    vcsProcess = false,
                    files = commitInfoMapper.mapDiffToFiles(stagedDiff),
                ),
                unstaged = UncommittedChangesState.Content.Unstaged(
                    vcsProcess = false,
                    files = commitInfoMapper.mapDiffToFiles(unstagedDiff),
                ),
            )
        }
    }

    private sealed interface Event {
        object Check : Event

        object AddAllToStaged : Event

        object RemoveAllFromStaged : Event

        data class AddFilesToStaged(val ids: Set<CommitFileId>) : Event

        data class RemoveFilesFromStaged(val ids: Set<CommitFileId>) : Event
    }
}
