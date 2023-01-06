package ru.ivk1800.diff.feature.bookmarks.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarksEvent
import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarksState

@Composable
@Preview
fun BookmarksView(
    state: BookmarksState,
    onEvent: (value: BookmarksEvent) -> Unit,
) = Scaffold(
    topBar = { AppBar(onEvent) }
) { Body(state, onEvent) }

@Composable
fun AppBar(
    onEvent: (value: BookmarksEvent) -> Unit,
) = TopAppBar(
    title = {
        Text(
            text = "Repositories",
            style = MaterialTheme.typography.h5
        )
    },
    elevation = 0.dp,
    backgroundColor = Color.Transparent,
    actions = {
        IconButton(
            onClick = {
                onEvent.invoke(BookmarksEvent.AddNewRepository)
            }
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }
)

@Composable
fun Body(
    state: BookmarksState,
    onEvent: (value: BookmarksEvent) -> Unit,
) = LazyColumn {
    items(
        state.items.size,
        key = { index -> state.items[index].id }
    ) { index ->
        val item = state.items[index]
        BookmarkItemView(
            item,
            onDelete = { onEvent.invoke(BookmarksEvent.OnDeleteBookmark(item.id)) },
            onClick = { onEvent.invoke(BookmarksEvent.OnOpenRepository(item.id)) }
        )
        Divider(
            startIndent = (8 + 8 + 32).dp,
        )
    }
}