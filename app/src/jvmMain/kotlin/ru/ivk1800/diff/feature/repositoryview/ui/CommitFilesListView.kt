package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ContextMenuState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ru.ivk1800.diff.compose.LocalDiffTheme
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.SelectedList
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.SelectedListState

@Composable
fun CommitFilesListView(
    modifier: Modifier = Modifier,
    items: ImmutableList<CommitFileItem>,
    lazyListState: LazyListState = rememberLazyListState(),
    state: SelectedListState<CommitFileId>
) {
    SelectedList<CommitFileId>(
        modifier,
        lazyListState = lazyListState,
        itemsCount = items.size,
        state = state,
        itemContent = { index ->
            val item = items[index]
            CommitFileItemViewWithMenu(
                modifier = Modifier,
                item = item,
            )
        },
    )
}

@Composable
private fun CommitFileItemViewWithMenu(
    modifier: Modifier = Modifier,
    item: CommitFileItem,
) {
    val state = remember { ContextMenuState() }

    ContextMenuArea(
        state = state,
        items = {
            listOf(
                ContextMenuItem(label = "Delete", onClick = {}),
            )
        },
    ) {
        CommitFileItemView(modifier, item)
    }
}

@Composable
private fun CommitFileItemView(
    modifier: Modifier = Modifier,
    item: CommitFileItem,
) = Row(
    modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    val commitFileTheme = LocalDiffTheme.current.commitFileTheme
    Box(
        modifier = Modifier.size(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                when (item.type) {
                    CommitFileItem.Type.Modified -> commitFileTheme.modifiedColor
                    CommitFileItem.Type.Added -> commitFileTheme.addedColor
                    CommitFileItem.Type.Renamed -> commitFileTheme.renamedColor
                    CommitFileItem.Type.Deleted -> commitFileTheme.deletedColor
                    CommitFileItem.Type.Copied -> Color.Unspecified // TODO
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = when (item.type) {
                CommitFileItem.Type.Modified -> "M"
                CommitFileItem.Type.Added -> "A"
                CommitFileItem.Type.Renamed -> "R"
                CommitFileItem.Type.Deleted -> "D"
                CommitFileItem.Type.Copied -> "C"
            },
            style = MaterialTheme.typography.caption,
            color = commitFileTheme.textColor,
        )
    }
    ListTextView(
        modifier = Modifier
            .padding(start = 4.dp)
            .weight(3F),
        text = item.name,
    )
}
