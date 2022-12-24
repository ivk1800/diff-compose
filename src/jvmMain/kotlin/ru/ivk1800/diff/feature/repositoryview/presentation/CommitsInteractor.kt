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
import ru.ivk1800.diff.feature.repositoryview.domain.Commit
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CommitsInteractor(
    private val repoDirectory: File,
    private val commitsRepository: CommitsRepository,
    private val commitItemMapper: CommitItemMapper,
) {
    // TODO: add main dispatcher
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val reloadEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private var commits = emptyList<Commit>()

    private val _state = MutableStateFlow<CommitsTableState>(CommitsTableState.Loading)
    val state: StateFlow<CommitsTableState>
        get() = _state

    init {
        reloadEvent
            // TODO: support throttle
            .onStart { emit(Unit) }
            .flatMapLatest {
                flow {
                    emit(CommitsTableState.Loading)
                    val newCommits = commitsRepository
                            .getCommits(repoDirectory, branchName = "master", limit = 20, offset = 0)
                    commits = newCommits
                    val items = newCommits.map(commitItemMapper::mapToItem).toImmutableList()
                    emit(CommitsTableState.Content(items))
                }
            }
            .onEach { state ->
                _state.value = state
            }.launchIn(scope)
    }

    fun getCommitHashByIndex(value: Int): String? = commits.getOrNull(value)?.hash?.value

    fun reload() {
        reloadEvent.tryEmit(Unit)
    }

    fun dispose() {
        scope.cancel()
    }
}
