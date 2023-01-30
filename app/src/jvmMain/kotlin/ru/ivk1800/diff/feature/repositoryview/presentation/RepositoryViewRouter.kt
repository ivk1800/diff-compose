package ru.ivk1800.diff.feature.repositoryview.presentation

import ru.ivk1800.diff.feature.repositoryview.RepositoryId
import java.io.File

interface RepositoryViewRouter {
    fun toTerminal(directory: File)

    fun toFinder(directory: File)

    fun close(id: RepositoryId)

    fun toPreferences()
}
