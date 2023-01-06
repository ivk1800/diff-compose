package ru.ivk1800.diff.feature.bookmarks.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class BookmarksState(val items: ImmutableList<BookmarkItem>)