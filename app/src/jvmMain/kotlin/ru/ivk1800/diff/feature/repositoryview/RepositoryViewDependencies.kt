package ru.ivk1800.diff.feature.repositoryview

import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewRouter
import ru.ivk1800.diff.logging.Logger
import ru.ivk1800.vcs.logged.LoggedVcs

data class RepositoryViewDependencies(
    val router: RepositoryViewRouter,
    val vcs: LoggedVcs,
    val logger: Logger,
)
