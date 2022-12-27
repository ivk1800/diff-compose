package ru.ivk1800.diff.feature.repositoryview.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ru.ivk1800.diff.compose.LocalDiffTheme
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem

@Composable
fun CommitFilesListView(
    modifier: Modifier = Modifier,
    items: ImmutableList<CommitFileItem>,
    onSelected: (event: SelectEvent) -> Unit,
    state: LazyListState = rememberLazyListState(),
) {
    List(
        modifier,
        state = state,
        itemsCount = items.size,
        itemContent = { index ->
            val item = items[index]
            CommitFileItemView(
                modifier = Modifier,
                item = item,
            )
        },
        onSelected = onSelected,
    )
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
