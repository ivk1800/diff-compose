package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import ru.ivk1800.diff.feature.repositoryview.domain.Commit
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitsTableState

@OptIn(ExperimentalCoroutinesApi::class)
class CommitsInteractor(
    private val commitsRepository: CommitsRepository,
    private val commitItemMapper: CommitItemMapper,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val eventsFlow = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    private var isLoading = false
    private var isAllLoaded = false

    private var commits = emptyList<Commit>()
    private var commitItems: PersistentList<CommitTableItem> = persistentListOf()

    private val selectedCommits = MutableStateFlow<ImmutableSet<CommitTableItem.Id.Commit>>(persistentSetOf())

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
                val commitsFlow = when (event) {
                    Event.Reload -> getInitialCommits()
                    Event.LoadMore -> getNextCommits()
                }

                commitsFlow.combine<ImmutableList<CommitTableItem>, ImmutableSet<CommitTableItem.Id.Commit>, CommitsTableState>(
                    selectedCommits
                ) { commits, selected ->
                    CommitsTableState.Content(
                        selected = selected,
                        commits = commits,
                    )
                }
                    .onStart {
                        if (event == Event.Reload) {
                            emit(CommitsTableState.Loading)
                        }
                    }
            }
            .onEach {
                isLoading = false
            }
            .onEach { state ->
                _state.value = state
            }.launchIn(scope)
    }

    private fun getInitialCommits(): Flow<ImmutableList<CommitTableItem>> =
        flow {
            val newCommits = commitsRepository
                .getCommits(branchName = "master", limit = 20, offset = 0)
            commits = newCommits
            val items = newCommits.map(commitItemMapper::mapToItem)
            commitItems = items.toPersistentList()
            emit(commitItems)
        }

    private fun getNextCommits(): Flow<ImmutableList<CommitTableItem>> =
        flow {
            val newCommits = commitsRepository
                .getCommits(branchName = "master", limit = 10, offset = commits.size)

            isAllLoaded = newCommits.isEmpty()

            commits += newCommits
            val items = newCommits.map(commitItemMapper::mapToItem)
            commitItems = commitItems.addAll(items)
            emit(commitItems)
        }

    fun getCommitHashByIndex(value: Int): String? = commits.getOrNull(value)?.hash?.value

    fun selectCommits(commits: ImmutableSet<CommitTableItem.Id.Commit>) {
        selectedCommits.value = commits
    }

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
