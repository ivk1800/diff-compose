package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.ivk1800.diff.MR
import ru.ivk1800.diff.application.ApplicationTheme
import ru.ivk1800.diff.compose.DiffTheme
import ru.ivk1800.diff.compose.LocalDiffTheme
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.SelectedList
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.rememberSelectedListState

@Composable
fun DiffInfoView(
    modifier: Modifier = Modifier,
    state: DiffInfoState,
) = Box(modifier = modifier) {
    when (state) {
        is DiffInfoState.Content -> DiffListView(items = state.items)
        DiffInfoState.None -> Unit
        is DiffInfoState.Error -> Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .align(Alignment.Center)
        ) {
            Text(
                text = state.message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@Composable
fun DiffListView(
    modifier: Modifier = Modifier,
    items: ImmutableList<DiffInfoItem>,
) {
    SelectedList<Int>(
        modifier,
        itemsCount = items.size,
        state = rememberSelectedListState(
            calculateIndex = { itemId -> itemId },
            calculateId = { index -> index },
        ),
        itemContent = { index ->
            when (val item = items[index]) {
                is DiffInfoItem.Line -> Line(item)

                is DiffInfoItem.HunkHeader -> HunkHeader(
                    item,
                    onActionClick = { },
                )
            }
        },
    )
}

@Composable
private fun HunkHeader(
    item: DiffInfoItem.HunkHeader,
    onActionClick: (DiffInfoItem.HunkHeader.Action) -> Unit,
) =
    Row(
        modifier = Modifier.fillMaxWidth().background(LocalDiffTheme.current.colors.header1Color),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ListTextView(
            // TODO: magic numbers
            modifier = Modifier.padding(start = (48 + 4).dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            text = item.text,
        )
        Row(
            // TODO: fixed height
            modifier = Modifier.height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item.actions.forEach { action ->
                HunkActionButton(
                    text = when (action) {
                        DiffInfoItem.HunkHeader.Action.StageHunk -> MR.strings.stage_hunk
                        DiffInfoItem.HunkHeader.Action.UnstageHunk -> MR.strings.unstage_hunk
                        DiffInfoItem.HunkHeader.Action.DiscardHunk -> MR.strings.discard_hunk
                        DiffInfoItem.HunkHeader.Action.DiscardLines -> MR.strings.discard_lines
                        DiffInfoItem.HunkHeader.Action.StageLines -> MR.strings.stage_lines
                        DiffInfoItem.HunkHeader.Action.UnstageLines -> MR.strings.unstage_lines
                    }.localized(),
                    onClick = { onActionClick.invoke(action) },
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }

@Preview
@Composable
private fun HunkHeaderPreview() {
    DiffTheme(ApplicationTheme.Dark) {
        HunkHeader(
            onActionClick = {},
            item = DiffInfoItem.HunkHeader(
                actions = persistentListOf(
                    DiffInfoItem.HunkHeader.Action.DiscardHunk
                ),
                text = "Test",
            )
        )
    }
}

@Composable
private fun Line(item: DiffInfoItem.Line) =
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when (item.type) {
                    DiffInfoItem.Line.Type.NotChanged -> Color.Transparent
                    DiffInfoItem.Line.Type.Added -> LocalDiffTheme.current.diffLinesTheme.addedColor
                    DiffInfoItem.Line.Type.Removed -> LocalDiffTheme.current.diffLinesTheme.removedColor
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val textColor = when (item.type) {
            DiffInfoItem.Line.Type.NotChanged -> Color.Unspecified
            DiffInfoItem.Line.Type.Added -> LocalDiffTheme.current.diffLinesTheme.addedTextColor
            DiffInfoItem.Line.Type.Removed -> LocalDiffTheme.current.diffLinesTheme.removedTextColor
        }
        ListTextView(
            modifier = Modifier.width(48.dp)
                .background(LocalDiffTheme.current.colors.header1Color)
                .padding(horizontal = 4.dp),
            text = buildString {
                if (item.number != null) {
                    append("${item.number} ")
                }
            },
            color = textColor,
        )
        Box(
            modifier = Modifier.width(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            ListTextView(
                text = when (item.type) {
                    DiffInfoItem.Line.Type.NotChanged -> ""
                    DiffInfoItem.Line.Type.Added -> "+"
                    DiffInfoItem.Line.Type.Removed -> "-"
                },
                color = textColor,
            )
        }
        ListTextView(
            text = item.text,
            color = textColor,
        )
    }
