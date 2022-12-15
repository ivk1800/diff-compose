package ru.ivk1800.diff.feature.bookmarks.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ru.ivk1800.diff.feature.bookmarks.domain.Bookmark
import ru.ivk1800.diff.feature.bookmarks.domain.BookmarksRepository

class BookmarksInteractor(
    private val bookmarksRepository: BookmarksRepository,
) {
    fun add(name: String, path: String): Bookmark {
        return bookmarksRepository.add(name, path)
    }

    fun delete(id: Int) = bookmarksRepository.delete(id)

    suspend fun getBookmarks(): ImmutableList<BookmarkItem> =
        bookmarksRepository.getBookmarks().map { bookmark ->
            BookmarkItem(
                id = bookmark.id,
                title = bookmark.name,
                subtitle = bookmark.path,
            )
        }.toImmutableList()
}