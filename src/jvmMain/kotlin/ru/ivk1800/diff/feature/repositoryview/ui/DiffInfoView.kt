package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import ru.ivk1800.diff.compose.LocalDiffTheme
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem

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
    List(
        modifier,
        itemsCount = items.size,
        itemContent = { index ->
            when (val item = items[index]) {
                is DiffInfoItem.Line ->
                    Row(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .background(
                                color = when (item.type) {
                                    DiffInfoItem.Line.Type.NotChanged -> Color.Transparent
                                    DiffInfoItem.Line.Type.Added -> LocalDiffTheme.current.diffLinesTheme.addedColor
                                    DiffInfoItem.Line.Type.Removed -> LocalDiffTheme.current.diffLinesTheme.removedColor
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
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
                            )
                        }
                        ListTextView(
                            text = item.text,
                        )
                    }

                is DiffInfoItem.HunkHeader -> Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ListTextView(
                        modifier = Modifier.padding(8.dp),
                        text = item.text,
                    )
                }
            }
        },
    )
}