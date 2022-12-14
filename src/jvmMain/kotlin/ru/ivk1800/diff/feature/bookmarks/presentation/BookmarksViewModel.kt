package ru.ivk1800.diff.feature.bookmarks.presentation

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.ivk1800.arch.presentation.BaseViewModel

class BookmarksViewModel : BaseViewModel() {

    private val _state = MutableStateFlow<BookmarksState>(BookmarksState.Loading)
    val state: StateFlow<BookmarksState>
        get() = _state

    init {
        viewModelScope.launch {
            delay(1_000)

            _state.value = BookmarksState.Content(
                items = persistentListOf(
                    BookmarkItem(
                        name = "Hello",
                        path = "/Users/Downloads"
                    ),
                    BookmarkItem(
                        name = "Hello",
                        path = "/Users/Downloads"
                    ),
                    BookmarkItem(
                        name = "Hello",
                        path = "/Users/Downloads"
                    )
                )
            )
        }
    }
}