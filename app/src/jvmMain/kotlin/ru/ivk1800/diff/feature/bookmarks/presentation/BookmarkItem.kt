package ru.ivk1800.diff.feature.bookmarks.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class BookmarkItem(val id: Int, val title: String, val subtitle: String)