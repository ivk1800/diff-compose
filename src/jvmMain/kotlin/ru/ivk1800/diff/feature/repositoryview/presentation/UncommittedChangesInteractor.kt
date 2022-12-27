package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class UncommittedChangesInteractor(
    private val repoDirectory: File,
    private val diffRepository: DiffRepository,
    private val commitInfoMapper: CommitInfoMapper,
) {
    // TODO: add main dispatcher
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val checkEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _state = MutableStateFlow<UncommittedChangesState>(
        UncommittedChangesState.None,
    )
    val state: StateFlow<UncommittedChangesState>
        get() = _state

    init {
        checkEvent
            .flatMapLatest {
                val stagedDiff: List<Diff> = diffRepository.getStagedDiff(repoDirectory)
                val notStagedDiff: List<Diff> = diffRepository.getNotStagedDiff(repoDirectory)

                if (stagedDiff.isEmpty() && notStagedDiff.isEmpty()) {
                    flowOf(UncommittedChangesState.None)
                } else {
                    flowOf(
                        UncommittedChangesState.Content(
                            staged = commitInfoMapper.mapDiffToFiles(stagedDiff),
                            notStaged = commitInfoMapper.mapDiffToFiles(notStagedDiff),
                        )
                    )
                }
            }
            .catch {
                // TODO handle errors
                emit(UncommittedChangesState.None)
            }
            .onEach { newState ->
                _state.value = newState
            }
            .launchIn(scope)
    }

    fun check() {
        scope.launch {
            checkEvent.emit(Unit)
        }
    }

    fun dispose() {
        scope.cancel()
    }
}
