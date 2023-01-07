package ru.ivk1800.diff.feature.repositoryview.di

import ru.ivk1800.diff.feature.repositoryview.RepositoryId
import ru.ivk1800.diff.feature.repositoryview.RepositoryViewDependencies
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.domain.FileRepository
import ru.ivk1800.diff.feature.repositoryview.domain.IndexRepository
import ru.ivk1800.diff.feature.repositoryview.domain.ObjectRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitsInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitsTableInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.FilesInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.IndexInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewRouter
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewViewModel
import ru.ivk1800.diff.feature.repositoryview.presentation.SelectionCoordinator
import ru.ivk1800.diff.feature.repositoryview.presentation.UncommittedChangesInteractor
import ru.ivk1800.diff.presentation.DialogRouter
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
            vcs = dependencies.vcs,
        )
    }

    private val diffRepository by lazy {
        DiffRepository(
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
            vcs = dependencies.vcs,
        )
    }

    private val indexRepository by lazy {
        IndexRepository(
            vcs = dependencies.vcs,
        )
    }

    private val diffInfoInteractor by lazy {
        DiffInfoInteractor(
            repoDirectory = repoPath,
            diffRepository = diffRepository,
            commitsRepository = commitsRepository,
            diffInfoItemMapper = DiffInfoItemMapper(),
        )
    }

    private val commitInfoMapper by lazy {
        CommitInfoMapper()
    }

    private val commitInfoInteractor by lazy {
        CommitInfoInteractor(
            repoDirectory = repoPath,
            commitsRepository = commitsRepository,
            commitInfoMapper = commitInfoMapper,
        )
    }

    private val commitsInteractor by lazy {
        CommitsInteractor(
            repoDirectory = repoPath,
            commitsRepository = commitsRepository,
            commitItemMapper = CommitItemMapper(),
        )
    }

    private val indexInteractor by lazy {
        IndexInteractor(
            repoDirectory = repoPath,
            objectRepository = objectRepository,
            fileRepository = fileRepository,
            indexRepository = indexRepository,
        )
    }

    private val uncommittedChangesInteractor by lazy {
        UncommittedChangesInteractor(
            repoDirectory = repoPath,
            diffRepository = diffRepository,
            commitInfoMapper = commitInfoMapper,
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

    val repositoryViewViewModel: RepositoryViewViewModel by lazy {
        RepositoryViewViewModel(
            repositoryDirectory = repoPath,
            commitInfoInteractor = commitInfoInteractor,
            diffInfoInteractor = diffInfoInteractor,
            filesInfoInteractor = filesInfoInteractor,
            uncommittedChangesInteractor = uncommittedChangesInteractor,
            selectionCoordinator = selectionCoordinator,
            commitsTableInteractor = commitsTableInteractor,
            indexInteractor = indexInteractor,
            router = dependencies.router,
            dialogRouter = object : DialogRouter {
                override fun show(dialog: DialogRouter.Dialog) {
                    dialogManager.show(dialog)
                }
            },
        )
    }

    fun dispose() {
        commitsInteractor.dispose()
        uncommittedChangesInteractor.dispose()
        commitInfoInteractor.dispose()
        diffInfoInteractor.dispose()
        commitsTableInteractor.dispose()
        filesInfoInteractor.dispose()
        selectionCoordinator.dispose()
    }
}
