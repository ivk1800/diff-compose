package ru.ivk1800.diff.feature.repositoryview.presentation

import java.io.File

interface RepositoryViewRouter {
    fun toTerminal(directory: File)

    fun toFinder(directory: File)
}