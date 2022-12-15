package ru.ivk1800.diff.feature.bookmarks.presentation

sealed interface BookmarksEvent {
    object AddNewRepository: BookmarksEvent
    data class OnDeleteBookmark(val id: Int): BookmarksEvent
}