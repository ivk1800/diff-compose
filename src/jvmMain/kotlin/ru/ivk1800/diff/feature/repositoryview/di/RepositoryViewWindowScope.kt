package ru.ivk1800.diff.feature.repositoryview.di

import ru.ivk1800.diff.feature.repositoryview.RepositoryId
import ru.ivk1800.diff.feature.repositoryview.RepositoryViewDependencies
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitsInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewRouter
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewViewModel
import ru.ivk1800.diff.feature.repositoryview.presentation.TableCommitsStateTransformer
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

    private val uncommittedChangesInteractor by lazy {
        UncommittedChangesInteractor(
            repoDirectory = repoPath,
            diffRepository = diffRepository,
            commitInfoMapper = commitInfoMapper,
        )
    }

    private val tableCommitsStateTransformer by lazy {
        TableCommitsStateTransformer()
    }

    val repositoryViewViewModel: RepositoryViewViewModel by lazy {
        RepositoryViewViewModel(
            repositoryDirectory = repoPath,
            commitsInteractor = commitsInteractor,
            commitInfoInteractor = commitInfoInteractor,
            diffInfoInteractor = diffInfoInteractor,
            uncommittedChangesInteractor = uncommittedChangesInteractor,
            tableCommitsStateTransformer = tableCommitsStateTransformer,
            router = dependencies.router,
            dialogRouter = object : DialogRouter {
                override fun show(dialog: DialogRouter.Dialog) {
                    dialogManager.show(dialog)
                }
            },
        )
    }
}
