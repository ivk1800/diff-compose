package ru.ivk1800.diff.feature.bookmarks.domain

import ru.ivk1800.diff.feature.bookmarks.data.BookmarksStorage

class BookmarksRepository(
    private val storage: BookmarksStorage,
) {
    fun add(name: String, path: String): Bookmark =
        storage.add(name = name, path = path).run {
            Bookmark(id, name, path)
        }

    fun getById(id: Int): Bookmark? =
        storage.getById(id)?.run {
            Bookmark(id, name, path)
        }

    fun delete(id: Int) = storage.delete(id)

    suspend fun getAll(): List<Bookmark> =
        storage.getAll().map { entity ->
            Bookmark(
                id = entity.id,
                name = entity.name,
                path = entity.path,
            )
        }
}