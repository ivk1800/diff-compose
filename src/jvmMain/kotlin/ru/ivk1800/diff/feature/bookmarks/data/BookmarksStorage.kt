package ru.ivk1800.diff.feature.bookmarks.data

class BookmarksStorage {
    private val bookmarks = mutableListOf<BookmarkEntity>(
        BookmarkEntity(id = 1, name = "Test", path = "/Users/ivan/repos/compose/diff-compose")
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

    fun getById(id: Int): BookmarkEntity? = bookmarks.firstOrNull { it.id == id }

    fun delete(id: Int) {
        val bookmark = bookmarks.firstOrNull { it.id == id }
        if (bookmark != null) {
            bookmarks.remove(bookmark)
        }
    }

    suspend fun getAll(): List<BookmarkEntity> {
        return bookmarks
    }
}