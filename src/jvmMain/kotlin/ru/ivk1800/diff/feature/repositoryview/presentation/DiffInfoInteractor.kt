package ru.ivk1800.diff.feature.repositoryview.presentation

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
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
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

    private val selectCommitEvent = MutableSharedFlow<SelectEvent?>(extraBufferCapacity = 1)

    private val _state = MutableStateFlow<DiffInfoState>(DiffInfoState.None)
    val state: StateFlow<DiffInfoState>
        get() = _state

    init {
        selectCommitEvent
            .flatMapLatest { event ->
                flow {
                    if (event != null) {
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
                    } else {
                        emit(DiffInfoState.None)
                    }

                }
            }
            .onEach { state ->
                _state.value = state
            }.launchIn(scope)
    }

    fun onFileSelected(commitHash: String, path: String) {
        selectCommitEvent.tryEmit(SelectEvent(commitHash = commitHash, path = path))
    }

    fun onFileUnselected() {
        selectCommitEvent.tryEmit(null)
    }

    fun dispose() {
        scope.cancel()
    }

    private data class SelectEvent(val commitHash: String, val path: String)
}
