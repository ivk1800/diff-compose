package ru.ivk1800.diff.di

import ru.ivk1800.diff.application.ApplicationThemeProvider
import ru.ivk1800.diff.feature.bookmarks.BookmarksWindowDependencies
import ru.ivk1800.diff.feature.bookmarks.BookmarksWindowFactory
import ru.ivk1800.diff.feature.preferences.PreferencesDependencies
import ru.ivk1800.diff.feature.preferences.PreferencesWindowFactory
import ru.ivk1800.diff.feature.preferences.presentation.PreferencesRouter
import ru.ivk1800.diff.feature.repositoryview.RepositoryViewDependencies
import ru.ivk1800.diff.feature.repositoryview.RepositoryViewWindowFactory
import ru.ivk1800.diff.logging.Logger
import ru.ivk1800.diff.navigation.BookmarksRouterImpl
import ru.ivk1800.diff.navigation.RepositoryViewRouterImpl
import ru.ivk1800.diff.window.WindowsManager
import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.git.GitVcs
import ru.ivk1800.vcs.logged.LoggedVcs

class ApplicationScope {
    val windowsManager: WindowsManager by lazy {
        WindowsManager()
    }

    private val vcsLogger: Logger by lazy {
        object : Logger {
            override fun e(error: Throwable, tag: String, message: String?) {
            }

            override fun d(tag: String, message: String) {
            }
        }
    }

    private val loggedVcs: LoggedVcs by lazy {
        LoggedVcs(
            target = GitVcs(vcsLogger),
        )
    }

    private val vcs: Vcs by lazy { loggedVcs }

    val bookmarksWindowFactory: BookmarksWindowFactory by lazy {
        BookmarksWindowFactory(
            dependencies = BookmarksWindowDependencies(
                router = BookmarksRouterImpl(windowsManager),
                vcs = vcs,
            ),
        )
    }

    val preferencesWindowFactory: PreferencesWindowFactory by lazy {
        PreferencesWindowFactory(
            dependencies = PreferencesDependencies(
                themeProvider = applicationThemeProvider,
                router = object : PreferencesRouter {
                    override fun close() = windowsManager.closePreferencesWindow()
                }
            )
        )
    }

    val repositoryViewWindowFactory: RepositoryViewWindowFactory by lazy {
        RepositoryViewWindowFactory(
            dependencies = RepositoryViewDependencies(
                router = RepositoryViewRouterImpl(windowsManager),
                vcs = loggedVcs,
                logger = object : Logger {
                    override fun e(error: Throwable, tag: String, message: String?) {
                        println(
                            buildString {
                                append("$tag: ")
                                if (message != null) {
                                    append("$message, ")
                                }
                                append(error)
                            }
                        )
                    }

                    override fun d(tag: String, message: String) {
                        println("$tag: $message")
                    }

                }
            )
        )
    }

    val applicationThemeProvider: ApplicationThemeProvider by lazy {
        ApplicationThemeProvider()
    }
}
