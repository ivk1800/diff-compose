package ru.ivk1800.diff.navigation

import ru.ivk1800.diff.feature.repositoryview.RepositoryId
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewRouter
import ru.ivk1800.diff.window.WindowsManager
import java.io.File
import java.util.concurrent.TimeUnit

class RepositoryViewRouterImpl(
    private val windowsManager: WindowsManager,
): RepositoryViewRouter  {
    override fun toTerminal(directory: File) {
        ProcessBuilder(*"open -a Terminal ${directory.path}".split(" ").toTypedArray())
            .start()
            .apply { waitFor(10, TimeUnit.SECONDS) }
    }

    override fun toFinder(directory: File) {
        ProcessBuilder(*"open -a Finder ${directory.path}".split(" ").toTypedArray())
            .start()
            .apply { waitFor(10, TimeUnit.SECONDS) }
    }

    override fun close(id: RepositoryId) {
        windowsManager.closeRepositoryWindow(id)
    }
}
