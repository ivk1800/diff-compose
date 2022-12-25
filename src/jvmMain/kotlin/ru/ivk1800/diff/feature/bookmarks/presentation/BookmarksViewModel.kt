package ru.ivk1800.diff.feature.bookmarks.presentation

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.ivk1800.diff.presentation.BaseViewModel
import ru.ivk1800.vcs.api.Vcs

class BookmarksViewModel(
    private val router: BookmarksRouter,
    private val vcs: Vcs,
    private val bookmarksInteractor: BookmarksInteractor,
) : BaseViewModel() {

    private val _state = MutableStateFlow<BookmarksState>(BookmarksState(persistentListOf()))
    val state: StateFlow<BookmarksState>
        get() = _state

    init {
        emitBookmarks()
    }

    fun onEvent(value: BookmarksEvent) {
        when (value) {
            BookmarksEvent.AddNewRepository -> {
                addNewRepository()
            }

            is BookmarksEvent.OnDeleteBookmark -> {
                bookmarksInteractor.delete(value.id)
                emitBookmarks()
            }

            is BookmarksEvent.OnOpenRepository -> {
                val repositoryPath = bookmarksInteractor.getBookmarkPath(value.id)
                if (repositoryPath != null) {
                    router.toRepository(repositoryPath)
                }
            }
        }
    }

    private fun addNewRepository() {
        router.toChooseRepositoryFolder { result ->
            viewModelScope.launch {
                if (vcs.isRepository(result)) {
                    bookmarksInteractor.add(
                        name = result.name,
                        path = result.path,
                    )
                    emitBookmarks()
                }
            }
        }
    }

    private fun emitBookmarks() {
        viewModelScope.launch {
            _state.value = BookmarksState(
                items = bookmarksInteractor.getBookmarks()
            )
        }
    }
}