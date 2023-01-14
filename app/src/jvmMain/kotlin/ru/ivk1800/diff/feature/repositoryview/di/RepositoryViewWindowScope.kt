package ru.ivk1800.diff.feature.repositoryview.di

import ru.ivk1800.diff.feature.repositoryview.RepositoryId
import ru.ivk1800.diff.feature.repositoryview.RepositoryViewDependencies
import ru.ivk1800.diff.feature.repositoryview.domain.ChangesRepository
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.domain.FileRepository
import ru.ivk1800.diff.feature.repositoryview.domain.ObjectRepository
import ru.ivk1800.diff.feature.repositoryview.domain.StatusRepository
import ru.ivk1800.diff.feature.repositoryview.domain.UncommittedRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.ChangesInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitsInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitsTableInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffOperationsInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.FilesInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEventHandler
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewRouter
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewViewModel
import ru.ivk1800.diff.feature.repositoryview.presentation.SelectionCoordinator
import ru.ivk1800.diff.feature.repositoryview.presentation.UncommittedChangesInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.state.composer.HistoryStateComposer
import ru.ivk1800.diff.feature.repositoryview.presentation.state.composer.RepositoryViewStateComposer
import ru.ivk1800.diff.feature.repositoryview.presentation.workspace.WorkspaceInteractor
import ru.ivk1800.diff.presentation.DialogRouter
import ru.ivk1800.diff.presentation.ErrorTransformer
import ru.ivk1800.diff.window.DialogManager
import java.io.File

class RepositoryViewWindowScope(
    private val dependencies: RepositoryViewDependencies,
    val id: RepositoryId,
) {
    private val repoPath: File = File(id.path)

    val dialogManager by lazy {
        DialogManager()
    }

    val router: RepositoryViewRouter
        get() = dependencies.router

    private val commitsRepository by lazy {
        CommitsRepository(
            repoDirectory = repoPath,
            vcs = dependencies.vcs,
        )
    }

    private val diffRepository by lazy {
        DiffRepository(
            repoDirectory = repoPath,
            vcs = dependencies.vcs,
        )
    }

    private val objectRepository by lazy {
        ObjectRepository(
            vcs = dependencies.vcs,
        )
    }

    private val fileRepository by lazy {
        FileRepository(
            repoDirectory = repoPath,
            vcs = dependencies.vcs,
        )
    }

    private val changesRepository by lazy {
        ChangesRepository(
            repoDirectory = repoPath,
            vcs = dependencies.vcs,
        )
    }

    private val diffInfoInteractor by lazy {
        DiffInfoInteractor(
            diffRepository = diffRepository,
            commitsRepository = commitsRepository,
            diffInfoItemMapper = DiffInfoItemMapper(),
            errorTransformer = errorTransformer,
        )
    }

    private val diffOperationsInteractor by lazy {
        DiffOperationsInteractor(
            filesInfoInteractor,
            diffInfoInteractor,
            changesInteractor,
        )
    }

    private val commitInfoMapper by lazy {
        CommitInfoMapper()
    }

    private val commitInfoInteractor by lazy {
        CommitInfoInteractor(
            commitsRepository = commitsRepository,
            commitInfoMapper = commitInfoMapper,
            errorTransformer = errorTransformer,
        )
    }

    private val commitsInteractor by lazy {
        CommitsInteractor(
            commitsRepository = commitsRepository,
            commitItemMapper = CommitItemMapper(),
        )
    }

    private val changesInteractor by lazy {
        ChangesInteractor(
            fileRepository = fileRepository,
            diffRepository = diffRepository,
            changesRepository = changesRepository,
        )
    }

    private val uncommittedRepository by lazy {
        UncommittedRepository(
            repoDirectory = repoPath,
            vcs = dependencies.vcs,
        )
    }

    private val statusRepository by lazy {
        StatusRepository(
            repoDirectory = repoPath,
            vcs = dependencies.vcs,
        )
    }

    private val uncommittedChangesInteractor by lazy {
        UncommittedChangesInteractor(
            statusRepository = statusRepository,
            uncommittedRepository = uncommittedRepository,
        )
    }

    private val commitsTableInteractor by lazy {
        CommitsTableInteractor(commitsInteractor, uncommittedChangesInteractor, diffInfoInteractor)
    }

    private val filesInfoInteractor by lazy {
        FilesInfoInteractor(commitInfoInteractor, uncommittedChangesInteractor, commitsTableInteractor)
    }

    private val selectionCoordinator by lazy {
        SelectionCoordinator(
            commitsTableInteractor,
            commitInfoInteractor,
            diffInfoInteractor,
            uncommittedChangesInteractor,
        )
    }

    private val errorTransformer by lazy {
        ErrorTransformer()
    }

    private val workspaceInteractor by lazy {
        WorkspaceInteractor()
    }

    private val historyStateComposer by lazy {
        HistoryStateComposer(
            filesInfoInteractor,
            commitsTableInteractor,
            diffInfoInteractor,
        )
    }

    private val repositoryViewStateComposer by lazy {
        RepositoryViewStateComposer(
            historyStateComposer,
            workspaceInteractor,
        )
    }

    private val repositoryViewEventHandler by lazy {
        RepositoryViewEventHandler(
            repositoryDirectory = repoPath,
            commitInfoInteractor = commitInfoInteractor,
            diffInfoInteractor = diffInfoInteractor,
            uncommittedChangesInteractor = uncommittedChangesInteractor,
            selectionCoordinator = selectionCoordinator,
            commitsTableInteractor = commitsTableInteractor,
            diffOperationsInteractor = diffOperationsInteractor,
            router = dependencies.router,
            errorTransformer = errorTransformer,
            dialogRouter = object : DialogRouter {
                override fun show(dialog: DialogRouter.Dialog) {
                    dialogManager.show(dialog)
                }
            },
            workspaceInteractor = workspaceInteractor,
        )
    }

    val repositoryViewViewModel: RepositoryViewViewModel by lazy {
        RepositoryViewViewModel(
            uncommittedChangesInteractor = uncommittedChangesInteractor,
            errorTransformer = errorTransformer,
            dialogRouter = object : DialogRouter {
                override fun show(dialog: DialogRouter.Dialog) {
                    dialogManager.show(dialog)
                }
            },
            repositoryViewEventHandler = repositoryViewEventHandler,
            repositoryViewStateComposer = repositoryViewStateComposer,
        )
    }

    fun dispose() {
        repositoryViewEventHandler.dispose()
        diffOperationsInteractor.dispose()
        commitsInteractor.dispose()
        uncommittedChangesInteractor.dispose()
        commitInfoInteractor.dispose()
        diffInfoInteractor.dispose()
        commitsTableInteractor.dispose()
        filesInfoInteractor.dispose()
        selectionCoordinator.dispose()
    }
}
