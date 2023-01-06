package ru.ivk1800.diff.feature.bookmarks.di.compose

import androidx.compose.runtime.staticCompositionLocalOf
import ru.ivk1800.diff.feature.bookmarks.di.BookmarksWindowScope

val LocalBookmarksWindowScope = staticCompositionLocalOf<BookmarksWindowScope> {
    throw IllegalStateException("BookmarksWindowScope not provided")
}
