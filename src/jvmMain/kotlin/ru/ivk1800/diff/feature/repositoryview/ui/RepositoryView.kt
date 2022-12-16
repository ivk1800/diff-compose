package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitItem

@Composable
fun RepositoryView(items: ImmutableList<CommitItem>) {
    Scaffold {
        Column {
            TopSections()
            CommitsTable(items)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CommitsTable(items: ImmutableList<CommitItem>) {
    var hovered by remember { mutableStateOf(-1) }

    val colors = MaterialTheme.colors
    val hoveredColor by remember { mutableStateOf(colors.onSurface.copy(alpha = 0.3f)) }

    LazyColumn {
        items(items.size) { index ->
            val item = items[index]
            val color by derivedStateOf {
                if (hovered == index) {
                    hoveredColor
                } else {
                    Color.Transparent
                }
            }

            CommitItemView(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color)
                    }
                    .onPointerEvent(PointerEventType.Exit) {
                        if (hovered == index) {
                            hovered = -1
                        }
                    }
                    .onPointerEvent(PointerEventType.Enter) {
                        hovered = index
                    },
                item = item,
            )
        }
    }
}

@Composable
private fun ColumnText(
    modifier: Modifier = Modifier,
    text: String,
) = Text(
    modifier = modifier,
    text = text,
    style = MaterialTheme.typography.caption,
    fontSize = 12.sp,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
)

@Composable
private fun SectionText(
    modifier: Modifier = Modifier,
    text: String,
) = Text(
    modifier = modifier,
    text = text,
    style = MaterialTheme.typography.subtitle2,
    textAlign = TextAlign.Center,
    fontSize = 12.sp,
)

@Composable
private fun TopSections() =
    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        SectionText(
            modifier = Modifier.weight(3F),
            text = "Description",
        )
        SectionDivider()
        val commonModifier = Modifier.weight(1F)
        SectionText(
            modifier = commonModifier,
            text = "Commit",
        )
        SectionDivider()
        SectionText(
            modifier = commonModifier,
            text = "Author",
        )
        SectionDivider()
        SectionText(
            modifier = commonModifier,
            text = "Date",
        )
    }

@Composable
private fun SectionDivider() =
    Box(
        modifier = Modifier.size(width = 1.dp, height = 16.dp)
            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.3f))
    )

@Composable
private fun CommitItemView(
    modifier: Modifier = Modifier,
    item: CommitItem,
) = Row(
    modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)
) {
    ColumnText(
        modifier = Modifier
            .padding(start = 4.dp)
            .weight(3F),
        text = item.description,
    )
    val commonModifier = Modifier
        .padding(start = 4.dp)
        .weight(1F)
    ColumnText(
        modifier = commonModifier,
        text = item.commit,
    )
    ColumnText(
        modifier = commonModifier,
        text = item.author,
    )
    ColumnText(
        modifier = commonModifier,
        text = item.date,
    )
}