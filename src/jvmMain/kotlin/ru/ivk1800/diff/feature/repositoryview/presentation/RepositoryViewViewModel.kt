package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow
import ru.ivk1800.arch.presentation.BaseViewModel

class RepositoryViewViewModel(
    private val commitsInteractor: CommitsInteractor,
): BaseViewModel() {

    val commits: StateFlow<ImmutableList<CommitItem>>
        get() = commitsInteractor.commits

    override fun dispose() {
        commitsInteractor.dispose()
        super.dispose()
    }
}
