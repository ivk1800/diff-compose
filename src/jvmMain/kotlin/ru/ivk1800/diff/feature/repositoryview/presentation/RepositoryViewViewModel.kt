package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.flow.StateFlow
import ru.ivk1800.arch.presentation.BaseViewModel
import java.io.File

class RepositoryViewViewModel(
    private val repositoryDirectory: File,
    private val commitsInteractor: CommitsInteractor,
    private val router: RepositoryViewRouter,
): BaseViewModel() {

    val state: StateFlow<RepositoryViewState>
        get() = commitsInteractor.state

    fun onEvent(value: RepositoryViewEvent) {
        when (value) {
            RepositoryViewEvent.OnReload -> commitsInteractor.reload()
            RepositoryViewEvent.OpenTerminal -> router.toTerminal(repositoryDirectory)
            RepositoryViewEvent.OpenFinder -> router.toFinder(repositoryDirectory)
        }
    }


    override fun dispose() {
        commitsInteractor.dispose()
        super.dispose()
    }
}
