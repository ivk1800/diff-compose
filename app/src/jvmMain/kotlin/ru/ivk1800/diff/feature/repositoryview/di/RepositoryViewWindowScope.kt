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
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewErrorHandler
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEventHandler
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewRouter
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewViewModel
import ru.ivk1800.diff.feature.repositoryview.presentation.SelectionCoordinator
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.ChangesManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitsManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitsTableManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.DiffInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.DiffOperationsManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.FilesInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.UncommittedChangesManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.WorkspaceManager
import ru.ivk1800.diff.feature.repositoryview.presentation.mapper.CommitInfoMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.mapper.CommitItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.mapper.DiffInfoItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.state.composer.FileStatusStateComposer
import ru.ivk1800.diff.feature.repositoryview.presentation.state.composer.HistoryStateComposer
import ru.ivk1800.diff.feature.repositoryview.presentation.state.composer.RepositoryViewStateComposer
import ru.ivk1800.diff.feature.repositoryview.presentation.state.composer.StashStateComposer
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

    private val diffInfoManager by lazy {
        DiffInfoManager(
            diffRepository = diffRepository,
            commitsRepository = commitsRepository,
            diffInfoItemMapper = DiffInfoItemMapper(),
            errorTransformer = errorTransformer,
        )
    }

    private val diffOperationsManager by lazy {
        DiffOperationsManager(
            filesInfoManager,
            diffInfoManager,
            changesManager,
        )
    }

    private val commitInfoMapper by lazy {
        CommitInfoMapper()
    }

    private val commitInfoManager by lazy {
        CommitInfoManager(
            commitsRepository = commitsRepository,
            commitInfoMapper = commitInfoMapper,
            errorTransformer = errorTransformer,
        )
    }

    private val commitsManager by lazy {
        CommitsManager(
            commitsRepository = commitsRepository,
            commitItemMapper = CommitItemMapper(),
        )
    }

    private val changesManager by lazy {
        ChangesManager(
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

    private val uncommittedChangesManager by lazy {
        UncommittedChangesManager(
            statusRepository = statusRepository,
            uncommittedRepository = uncommittedRepository,
        )
    }

    private val commitsTableManager by lazy {
        CommitsTableManager(commitsManager, uncommittedChangesManager, diffInfoManager)
    }

    private val filesInfoManager by lazy {
        FilesInfoManager(commitInfoManager, uncommittedChangesManager, commitsTableManager)
    }

    private val selectionCoordinator by lazy {
        SelectionCoordinator(
            commitsTableManager,
            commitInfoManager,
            diffInfoManager,
            uncommittedChangesManager,
        )
    }

    private val errorTransformer by lazy {
        ErrorTransformer()
    }

    private val workspaceManager by lazy {
        WorkspaceManager()
    }

    private val historyStateComposer by lazy {
        HistoryStateComposer(
            filesInfoManager,
            commitsTableManager,
            diffInfoManager,
        )
    }

    private val fileStatusStateComposer by lazy {
        FileStatusStateComposer(
            uncommittedChangesManager,
            diffInfoManager,
        )
    }

    private val repositoryViewStateComposer by lazy {
        RepositoryViewStateComposer(
            historyStateComposer,
            fileStatusStateComposer,
            workspaceManager,
            stashStateComposer,
        )
    }

    private val stashStateComposer by lazy {
        StashStateComposer()
    }

    private val repositoryViewEventHandler by lazy {
        RepositoryViewEventHandler(
            repositoryDirectory = repoPath,
            commitInfoManager = commitInfoManager,
            diffInfoManager = diffInfoManager,
            uncommittedChangesManager = uncommittedChangesManager,
            selectionCoordinator = selectionCoordinator,
            commitsTableManager = commitsTableManager,
            diffOperationsManager = diffOperationsManager,
            router = dependencies.router,
            dialogRouter = object : DialogRouter {
                override fun show(dialog: DialogRouter.Dialog) {
                    dialogManager.show(dialog)
                }
            },
            errorHandler = repositoryViewErrorHandler,
            workspaceManager = workspaceManager,
        )
    }

    val repositoryViewViewModel: RepositoryViewViewModel by lazy {
        RepositoryViewViewModel(
            uncommittedChangesManager = uncommittedChangesManager,
            eventHandler = repositoryViewEventHandler,
            repositoryViewStateComposer = repositoryViewStateComposer,
        )
    }

    private val repositoryViewErrorHandler: RepositoryViewErrorHandler by lazy {
        RepositoryViewErrorHandler(
            uncommittedChangesManager = uncommittedChangesManager,
            errorTransformer = errorTransformer,
            commitsManager = commitsManager,
            dialogRouter = object : DialogRouter {
                override fun show(dialog: DialogRouter.Dialog) {
                    dialogManager.show(dialog)
                }
            },
        )
    }

    fun dispose() {
        repositoryViewErrorHandler.dispose()
        repositoryViewEventHandler.dispose()
        diffOperationsManager.dispose()
        commitsManager.dispose()
        uncommittedChangesManager.dispose()
        commitInfoManager.dispose()
        diffInfoManager.dispose()
        commitsTableManager.dispose()
        filesInfoManager.dispose()
        selectionCoordinator.dispose()
    }
}
