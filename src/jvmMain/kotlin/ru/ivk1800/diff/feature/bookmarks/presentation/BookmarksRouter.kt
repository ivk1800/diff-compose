package ru.ivk1800.diff.feature.bookmarks.presentation

import java.io.File

interface BookmarksRouter {
    fun toChooseRepositoryFolder(callback: (value: File) -> Unit)

    fun toRepository(path: String)
}