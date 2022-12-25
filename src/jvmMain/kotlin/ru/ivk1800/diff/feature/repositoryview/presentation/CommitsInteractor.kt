package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import ru.ivk1800.diff.feature.repositoryview.domain.Commit
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitItem
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CommitsInteractor(
    private val repoDirectory: File,
    private val commitsRepository: CommitsRepository,
    private val commitItemMapper: CommitItemMapper,
) {
    // TODO: add main dispatcher
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val eventsFlow = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    private var isLoading = false
    private var isAllLoaded = false

    private var commits = emptyList<Commit>()
    private var commitItems: PersistentList<CommitItem> = persistentListOf<CommitItem>()

    private val _state = MutableStateFlow<CommitsTableState>(CommitsTableState.Loading)
    val state: StateFlow<CommitsTableState>
        get() = _state

    init {
        eventsFlow
            // TODO: support throttle
            .onStart { emit(Event.Reload) }
            .filter { !isLoading }
            .filter { event ->
                if (event == Event.LoadMore) {
                    !isAllLoaded
                } else {
                    true
                }
            }
            .onEach { event ->
                if (event == Event.Reload) {
                    isAllLoaded = false
                }
                isLoading = true
            }
            .flatMapLatest { event ->
                when (event) {
                    Event.Reload -> getInitialCommits()
                    Event.LoadMore -> getNextCommits()
                }
            }
            .onEach {
                isLoading = false
            }
            .onEach { state ->
                _state.value = state
            }.launchIn(scope)
    }

    private fun getInitialCommits() =
        flow {
            emit(CommitsTableState.Loading)
            val newCommits = commitsRepository
                .getCommits(repoDirectory, branchName = "master", limit = 20, offset = 0)
            commits = newCommits
            val items = newCommits.map(commitItemMapper::mapToItem)
            commitItems = items.toPersistentList()
            emit(CommitsTableState.Content(commitItems))
        }

    private fun getNextCommits() =
        flow {
            val newCommits = commitsRepository
                .getCommits(repoDirectory, branchName = "master", limit = 10, offset = commits.size)

            isAllLoaded = newCommits.isEmpty()

            commits += newCommits
            val items = newCommits.map(commitItemMapper::mapToItem)
            commitItems = commitItems.addAll(items)
            emit(CommitsTableState.Content(commitItems))
        }

    fun getCommitHashByIndex(value: Int): String? = commits.getOrNull(value)?.hash?.value

    fun reload() {
        eventsFlow.tryEmit(Event.Reload)
    }

    fun loadMore() {
        eventsFlow.tryEmit(Event.LoadMore)
    }

    fun dispose() {
        scope.cancel()
    }

    private enum class Event {
        Reload,
        LoadMore,
    }
}
