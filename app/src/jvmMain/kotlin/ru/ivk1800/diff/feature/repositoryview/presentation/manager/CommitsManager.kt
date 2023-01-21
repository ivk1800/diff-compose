package ru.ivk1800.diff.feature.repositoryview.presentation.manager

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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.domain.Commit
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.domain.StatusRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEventHandler
import ru.ivk1800.diff.feature.repositoryview.presentation.mapper.CommitItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitsTableState
import ru.ivk1800.diff.logging.Logger
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class CommitsManager(
    private val statusRepository: StatusRepository,
    private val commitsRepository: CommitsRepository,
    private val commitItemMapper: CommitItemMapper,
    private val logger: Logger,
    context: CoroutineContext,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    private val eventsFlow = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    private var isLoading = false
    private var isAllLoaded = false

    private var commits = emptyList<Commit>()
    private var commitItems: PersistentList<CommitTableItem> = persistentListOf()

    private val selectedCommits = MutableStateFlow<ImmutableSet<CommitTableItem.Id.Commit>>(persistentSetOf())

    private val _state = MutableStateFlow<CommitsTableState>(CommitsTableState.Loading)
    val state: StateFlow<CommitsTableState>
        get() = _state

    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 1)
    val errors: Flow<Throwable>
        get() = _errors

    constructor(
        statusRepository: StatusRepository,
        commitsRepository: CommitsRepository,
        commitItemMapper: CommitItemMapper,
        logger: Logger,
    ) : this(
        statusRepository,
        commitsRepository,
        commitItemMapper,
        logger,
        Dispatchers.Main,
    )

    init {
        eventsFlow
            // TODO: support throttle
            .onStart { emit(Event.Reload) }
            .onEach {
                logger.d(tag = Tag, message = "Received event: $it")
            }
            .filter {
                val proceed = !isLoading
                logger.d(tag = Tag, message = "Proceed event ($it): $proceed")
                proceed
            }
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
                logger.d(tag = Tag, message = "Loading start")
                isLoading = true
            }
            .flatMapLatest { event ->
                val commitsFlow = when (event) {
                    Event.Reload -> getInitialCommits()
                    Event.LoadMore -> getNextCommits()
                }
                    .onEach {
                        logger.d(tag = Tag, message = "Loading end")
                        isLoading = false
                    }

                commitsFlow.combine<ImmutableList<CommitTableItem>, ImmutableSet<CommitTableItem.Id.Commit>, CommitsTableState>(
                    selectedCommits
                        .onEach {
                            logger.d(tag = Tag, message = "Received new selected commits: $it")
                        }
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
                    .catch { error ->
                        logger.e(
                            tag = Tag,
                            error = error,
                            message = "An error occurred while loading commits",
                        )
                        _errors.emit(error)
                    }
            }
            .onEach { state ->
                logger.d(tag = Tag, message = "Emit new state: ${state::class.simpleName}")
                _state.value = state
            }
            .launchIn(scope)
    }

    private fun getInitialCommits(): Flow<ImmutableList<CommitTableItem>> =
        flow {
            val branchName = getCurrentBranch()
            logger.d(tag = Tag, message = "Get initial commits: branchName: $branchName")
            val newCommits = commitsRepository
                .getCommits(
                    branchName = branchName,
                    limit = CommitsLimit,
                    afterCommit = null,
                )
            commits = newCommits
            val items = newCommits.map(commitItemMapper::mapToItem)
            commitItems = items.toPersistentList()
            emit(commitItems)
        }

    private fun getNextCommits(): Flow<ImmutableList<CommitTableItem>> =
        flow {
            val branchName = getCurrentBranch()
            logger.d(tag = Tag, message = "Get next commits: branchName: $branchName")
            val newCommits = commitsRepository
                .getCommits(
                    branchName = branchName,
                    limit = CommitsLimit,
                    afterCommit = requireNotNull(commits.lastOrNull()?.hash?.value),
                )

            isAllLoaded = newCommits.isEmpty()

            commits += newCommits
            val items = newCommits.map(commitItemMapper::mapToItem)
            commitItems = commitItems.addAll(items)
            emit(commitItems)
        }

    fun selectCommits(commits: ImmutableSet<CommitTableItem.Id.Commit>) {
        logger.d(tag = Tag, message = "Select commits: $commits")
        selectedCommits.value = commits
    }

    fun reload() {
        logger.d(tag = Tag, message = "Reload")
        scope.launch { eventsFlow.emit(Event.Reload) }
    }

    fun loadMore() {
        logger.d(tag = Tag, message = "Load more")
        scope.launch { eventsFlow.emit(Event.LoadMore) }
    }

    fun dispose() {
        logger.d(tag = Tag, message = "dispose")
        scope.cancel()
    }

    private suspend fun getCurrentBranch() = statusRepository.getStatus().branch

    private enum class Event {
        Reload,
        LoadMore,
    }

    private companion object {
        private const val CommitsLimit = 50
        private val Tag = RepositoryViewEventHandler::class.simpleName.orEmpty()
    }
}
