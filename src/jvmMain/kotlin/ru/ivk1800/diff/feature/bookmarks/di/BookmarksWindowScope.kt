package ru.ivk1800.diff.feature.bookmarks.di

import ru.ivk1800.diff.feature.bookmarks.BookmarksWindowDependencies
import ru.ivk1800.diff.feature.bookmarks.data.BookmarksStorage
import ru.ivk1800.diff.feature.bookmarks.domain.BookmarksRepository
import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarksInteractor
import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarksViewModel

class BookmarksWindowScope(
    private val dependencies: BookmarksWindowDependencies,
) {
    val bookmarksViewModel: BookmarksViewModel by lazy {
        BookmarksViewModel(
            router = dependencies.router,
            vcs = dependencies.vcs,
            bookmarksInteractor = BookmarksInteractor(
                bookmarksRepository = BookmarksRepository(
                    storage = BookmarksStorage(),
                ),
            )
        )
    }
}
