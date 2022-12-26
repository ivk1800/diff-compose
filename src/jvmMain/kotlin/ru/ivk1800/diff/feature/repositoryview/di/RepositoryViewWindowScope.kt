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
import ru.ivk1800.diff.feature.repositoryview.presentation.UncommittedChangesInteractor
import java.io.File

class RepositoryViewWindowScope(
    private val dependencies: RepositoryViewDependencies,
    val id: RepositoryId,
) {
    private val repoPath: File = File(id.path)

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

    private val commitInfoInteractor by lazy {
        CommitInfoInteractor(
            repoDirectory = repoPath,
            commitsRepository = commitsRepository,
            commitInfoMapper = CommitInfoMapper(),
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
        UncommittedChangesInteractor()
    }

    val repositoryViewViewModel: RepositoryViewViewModel by lazy {
        RepositoryViewViewModel(
            repositoryDirectory = repoPath,
            commitsInteractor = commitsInteractor,
            commitInfoInteractor = commitInfoInteractor,
            diffInfoInteractor = diffInfoInteractor,
            uncommittedChangesInteractor = uncommittedChangesInteractor,
            router = dependencies.router,
        )
    }
}
