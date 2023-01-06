package ru.ivk1800.diff.feature.bookmarks.presentation

import ru.ivk1800.diff.feature.repositoryview.RepositoryId
import java.io.File

interface BookmarksRouter {
    fun toChooseRepositoryFolder(callback: (value: File) -> Unit)

    fun toRepository(id: RepositoryId)
}