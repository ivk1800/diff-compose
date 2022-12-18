package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.toImmutableList
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
import kotlinx.coroutines.flow.onStart
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CommitInfoInteractor(
    private val repoDirectory: File,
    private val commitsRepository: CommitsRepository,
) {
    // TODO: add main dispatcher
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val selectCommitEvent = MutableSharedFlow<String?>(extraBufferCapacity = 1)

    private val _state = MutableStateFlow<CommitInfoState>(CommitInfoState.None)
    val state: StateFlow<CommitInfoState>
        get() = _state

    init {
        selectCommitEvent
            .flatMapLatest { hash ->
                flow {
                    emit(CommitInfoState.None)
                    if (hash != null) {
                        val files = commitsRepository
                            .getCommitFiles(repoDirectory, commitHash = hash)
                        val items = files.map { file ->
                            CommitFileItem(
                                name = file.path,
                                type = CommitFileItem.Type.Added,
                            )
                        }.toImmutableList()
                        emit(CommitInfoState.Content(items))
                    }
                }
            }
            .onEach { state ->
                _state.value = state
            }.launchIn(scope)
    }

    fun onCommitSelected(hash: String?) {
        selectCommitEvent.tryEmit(hash)
    }

    fun dispose() {
        scope.cancel()
    }
}
