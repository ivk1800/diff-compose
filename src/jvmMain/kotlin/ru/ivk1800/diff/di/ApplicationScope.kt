package ru.ivk1800.diff.di

import ru.ivk1800.diff.application.ApplicationThemeProvider
import ru.ivk1800.diff.feature.bookmarks.BookmarksWindowDependencies
import ru.ivk1800.diff.feature.bookmarks.BookmarksWindowFactory
import ru.ivk1800.diff.feature.repositoryview.RepositoryViewDependencies
import ru.ivk1800.diff.feature.repositoryview.RepositoryViewWindowFactory
import ru.ivk1800.diff.navigation.BookmarksRouterImpl
import ru.ivk1800.diff.navigation.RepositoryViewRouterImpl
import ru.ivk1800.diff.window.WindowsManager
import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.git.GitVcs

class ApplicationScope {
    val windowsManager: WindowsManager by lazy {
        WindowsManager()
    }

    private val vcs: Vcs by lazy { GitVcs() }

    val bookmarksWindowFactory: BookmarksWindowFactory by lazy {
        BookmarksWindowFactory(
            dependencies = BookmarksWindowDependencies(
                router = BookmarksRouterImpl(windowsManager),
                vcs = vcs,
            ),
        )
    }

    val repositoryViewWindowFactory: RepositoryViewWindowFactory by lazy {
        RepositoryViewWindowFactory(
            dependencies = RepositoryViewDependencies(
                router = RepositoryViewRouterImpl(windowsManager),
                vcs = vcs,
            )
        )
    }

    val applicationThemeProvider: ApplicationThemeProvider by lazy {
        ApplicationThemeProvider()
    }
}
