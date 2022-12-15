package ru.ivk1800.diff.feature.bookmarks.data

class BookmarksStorage {
    private val bookmarks = mutableListOf<BookmarkEntity>(
        BookmarkEntity(id = 1, name = "Test", path = "/Users/admin/test")
    )

    fun add(name: String, path: String): BookmarkEntity {
        val bookmark = BookmarkEntity(
            id = bookmarks.size + 1,
            name = name,
            path = path,
        )
        bookmarks.add(bookmark)
        return bookmark
    }

    fun delete(id: Int) {
        val bookmark = bookmarks.firstOrNull { it.id == id }
        if (bookmark != null) {
            bookmarks.remove(bookmark)
        }
    }

    suspend fun getBookmarks(): List<BookmarkEntity> {
        return bookmarks
    }
}