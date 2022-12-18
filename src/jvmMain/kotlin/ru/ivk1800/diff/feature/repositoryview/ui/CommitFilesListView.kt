package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem

@Composable
fun CommitFilesListView(
    modifier: Modifier = Modifier,
    items: ImmutableList<CommitFileItem>,
) {
    List(
        modifier,
        itemsCount = items.size,
        itemContent = { index ->
            val item = items[index]
            CommitFileItemView(
                modifier = Modifier,
                item = item,
            )
        },
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
    Box(
        modifier = Modifier.size(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                when (item.type) {
                    CommitFileItem.Type.Edited -> Color(0xFFf1940b)
                    CommitFileItem.Type.Added -> Color(0xFF4fad08)
                    CommitFileItem.Type.Moved -> Color(0xFF418df6)
                }
            ),
    ) {
        Icon(
            modifier = Modifier.size(12.dp).align(Alignment.Center),
            imageVector = when (item.type) {
                CommitFileItem.Type.Edited -> Icons.Filled.Edit
                CommitFileItem.Type.Added -> Icons.Filled.Add
                CommitFileItem.Type.Moved -> Icons.Filled.List
            },
            contentDescription = "",
            tint = Color.Black,
        )
    }
    ListTextView(
        modifier = Modifier
            .padding(start = 4.dp)
            .weight(3F),
        text = item.name,
    )
}
