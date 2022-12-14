package ru.ivk1800.diff.feature.bookmarks.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface BookmarksState {
    object Loading : BookmarksState

    data class Content(val items: ImmutableList<BookmarkItem>) : BookmarksState
}