package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.flow.StateFlow
import ru.ivk1800.arch.presentation.BaseViewModel

class RepositoryViewViewModel(
    private val commitsInteractor: CommitsInteractor,
): BaseViewModel() {

    val state: StateFlow<RepositoryViewState>
        get() = commitsInteractor.state

    fun onEvent(value: RepositoryViewEvent) {
        when (value) {
            RepositoryViewEvent.OnReload -> commitsInteractor.reload()
        }
    }


    override fun dispose() {
        commitsInteractor.dispose()
        super.dispose()
    }
}
