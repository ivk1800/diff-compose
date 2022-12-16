package ru.ivk1800.diff.feature.bookmarks.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarkItem

@Composable
@Preview
fun BookmarkItemView(
    item: BookmarkItem,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) =
    ContextMenuArea(
        items = {
            listOf(
                ContextMenuItem(label = "Delete", onClick = onDelete),
            )
        },
    ) {
        BookmarkItem(item, onClick)
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview
fun BookmarkItem(
    item: BookmarkItem,
    onClick: () -> Unit,
) =
    Row(
        modifier = Modifier
            .onClick(
                matcher = PointerMatcher.mouse(PointerButton.Primary),
                onDoubleClick = onClick,
                onClick = { }
            )
            .padding(4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier,
                text = "git",
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.Center,
                color = Color.White,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                style = MaterialTheme.typography.subtitle1,
                text = item.title,
            )
            Text(
                style = MaterialTheme.typography.caption,
                text = item.subtitle,
            )
        }
    }