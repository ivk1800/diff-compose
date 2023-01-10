package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.domain.Commit
import ru.ivk1800.diff.feature.repositoryview.domain.CommitFile
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitId
import ru.ivk1800.diff.presentation.ErrorTransformer
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CommitInfoInteractor(
    private val repoDirectory: File,
    private val commitsRepository: CommitsRepository,
    private val commitInfoMapper: CommitInfoMapper,
    private val errorTransformer: ErrorTransformer,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val selectedFiles = MutableStateFlow<ImmutableSet<CommitFileId>>(persistentSetOf())
    private var files = emptyList<CommitFile>()
    private var commit: Commit? = null

    private val selectCommitEvent = MutableSharedFlow<CommitId?>(
        replay = 1,
        extraBufferCapacity = 1,
    )

    private val _state = MutableStateFlow<CommitInfoState>(CommitInfoState.None)
    val state: StateFlow<CommitInfoState>
        get() = _state

    val selectedCommitHash: String?
        get() = selectCommitEvent.replayCache.firstOrNull()?.hash

    init {
        selectCommitEvent
            .distinctUntilChanged()
            .onEach {
                selectFiles(persistentSetOf())
            }
            .flatMapLatest { id ->
                val hash = id?.hash
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
                                selected = persistentSetOf(),
                                files = commitInfoMapper.mapToFiles(newFiles),
                                description = commitInfoMapper.mapToDescription(newCommit),
                            )
                        )
                    }
                }
                    .combine(selectedFiles) { state, newSelected ->
                        when (state) {
                            is CommitInfoState.Content -> state.copy(
                                selected = newSelected,
                            )

                            CommitInfoState.None,
                            is CommitInfoState.Error -> state
                        }
                    }
                    .catch { error ->
                        emit(CommitInfoState.Error(message = errorTransformer.transformForDisplay(error)))
                    }
            }
            .onEach { state ->
                _state.value = state
            }.launchIn(scope)
    }

    fun selectFiles(files: ImmutableSet<CommitFileId>) {
        selectedFiles.value = files
    }

    fun selectCommit(id: CommitId?) {
        scope.launch {
            selectCommitEvent.emit(id)
        }
    }

    fun dispose() {
        scope.cancel()
    }
}
