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
import ru.ivk1800.diff.feature.repositoryview.domain.Commit
import ru.ivk1800.diff.feature.repositoryview.domain.CommitFile
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CommitInfoInteractor(
    private val repoDirectory: File,
    private val commitsRepository: CommitsRepository,
    private val commitInfoMapper: CommitInfoMapper,
) {
    // TODO: add main dispatcher
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    private var files = emptyList<CommitFile>()
    private var commit: Commit? = null

    private val selectCommitEvent = MutableSharedFlow<String?>(
        replay = 1,
        extraBufferCapacity = 1,
    )

    private val _state = MutableStateFlow<CommitInfoState>(CommitInfoState.None)
    val state: StateFlow<CommitInfoState>
        get() = _state

    val selectedCommitHash: String?
        get() = selectCommitEvent.replayCache.firstOrNull()

    init {
        selectCommitEvent
            .flatMapLatest { hash ->
                flow {
                    emit(CommitInfoState.None)
                    if (hash != null) {
                        val newFiles = commitsRepository
                            .getCommitFiles(repoDirectory, commitHash = hash)
                        files = newFiles

                        val newCommit = requireNotNull(commitsRepository.getCommit(repoDirectory, hash))
                        commit = newCommit

                        emit(
                            CommitInfoState.Content(
                                files = commitInfoMapper.mapToFiles(newFiles),
                                description = commitInfoMapper.mapToDescription(newCommit),
                            )
                        )
                    }
                }
            }
            .onEach { state ->
                _state.value = state
            }.launchIn(scope)
    }

    fun getFilePathByIndex(value: Int): String? = files.getOrNull(value)?.path

    fun onCommitSelected(hash: String?) {
        selectCommitEvent.tryEmit(hash)
    }

    fun dispose() {
        scope.cancel()
    }
}
