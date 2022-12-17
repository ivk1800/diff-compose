package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import java.io.File

class CommitsInteractor(
    private val repoDirectory: File,
    private val commitsRepository: CommitsRepository,
    private val commitItemMapper: CommitItemMapper,
) {
    // TODO: add main dispatcher
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())

    private val _commits = MutableStateFlow<ImmutableList<CommitItem>>(persistentListOf())
    val commits: StateFlow<ImmutableList<CommitItem>>
        get() = _commits

    init {
        scope.launch {
            val commits = commitsRepository.getCommits(repoDirectory, branchName = "master", limit = 20, offset = 0)
            _commits.value = commits.map (commitItemMapper::mapToItem).toImmutableList()
        }
    }

    fun dispose() {
        scope.cancel()
    }
}
