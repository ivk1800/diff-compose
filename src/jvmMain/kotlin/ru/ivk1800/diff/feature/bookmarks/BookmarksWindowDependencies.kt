package ru.ivk1800.diff.feature.bookmarks

import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarksRouter
import ru.ivk1800.vcs.api.Vcs

data class BookmarksWindowDependencies(
    val router: BookmarksRouter,
    val vcs: Vcs,
)
