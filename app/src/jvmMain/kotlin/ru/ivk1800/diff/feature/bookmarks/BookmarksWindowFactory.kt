package ru.ivk1800.diff.feature.bookmarks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import ru.ivk1800.diff.feature.bookmarks.di.BookmarksWindowScope
import ru.ivk1800.diff.feature.bookmarks.di.compose.LocalBookmarksWindowScope
import ru.ivk1800.diff.feature.bookmarks.ui.BookmarksWindow

class BookmarksWindowFactory(
    private val dependencies: BookmarksWindowDependencies,
) {
    @Composable
    fun create() {
        val scope = remember { BookmarksWindowScope(dependencies) }

        CompositionLocalProvider(LocalBookmarksWindowScope provides scope) {
            BookmarksWindow(onCloseRequest = { })
        }
    }
}
