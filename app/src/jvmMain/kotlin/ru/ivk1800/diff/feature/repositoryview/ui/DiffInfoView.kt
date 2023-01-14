package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.MR
import ru.ivk1800.diff.application.ApplicationTheme
import ru.ivk1800.diff.compose.DiffTheme
import ru.ivk1800.diff.compose.LocalDiffTheme
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.DiffInfoState
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.SelectedList
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.rememberSelectedListState

@Composable
fun DiffInfoView(
    modifier: Modifier = Modifier,
    state: DiffInfoState,
    onEvent: (value: RepositoryViewEvent.Diff) -> Unit,
) = Box(modifier = modifier) {
    when (state) {
        is DiffInfoState.Content -> DiffListView(
            onEvent = onEvent,
            items = state.items,
            selected = state.selected,
        )

        DiffInfoState.None -> Unit
        is DiffInfoState.Error -> ErrorMessageView(state.message)
    }
}

@Composable
fun DiffListView(
    modifier: Modifier = Modifier,
    items: ImmutableList<DiffInfoItem>,
    selected: ImmutableSet<DiffInfoItem.Id.Line>,
    onEvent: (value: RepositoryViewEvent.Diff) -> Unit,
) {
    val currentSelected by rememberUpdatedState(selected)
    val currentItems by rememberUpdatedState(items)
    val currentOnEvent by rememberUpdatedState(onEvent)
    val selectableListState = rememberSelectedListState(
        onSelect = { selectedItems ->
            currentOnEvent.invoke(
                RepositoryViewEvent.Diff.OnLinesSelected(
                    selectedItems.filterIsInstance<DiffInfoItem.Id.Line>().toImmutableSet()
                )
            )
            false
        },
        onInteractable = { id -> id is DiffInfoItem.Id.Line },
        calculateIndex = { itemId -> currentItems.indexOfFirst { it.id == itemId } },
        calculateId = { index -> currentItems[index].id },
    )

    LaunchedEffect(key1 = selectableListState) {
        snapshotFlow { currentSelected }
            .onEach(selectableListState::select)
            .launchIn(this)
    }

    SelectedList(
        modifier,
        itemsCount = items.size,
        state = selectableListState,
        itemContent = { index ->
            when (val item = items[index]) {
                is DiffInfoItem.Line -> Line(item)

                is DiffInfoItem.HunkHeader -> HunkHeader(
                    item,
                    onActionClick = { action ->
                        when (action) {
                            DiffInfoItem.HunkHeader.Action.StageHunk -> Unit
                            DiffInfoItem.HunkHeader.Action.UnstageHunk ->
                                onEvent.invoke(
                                    RepositoryViewEvent.Diff.OnUnstageHunk(
                                        hunkId = item.id,
                                    ),
                                )

                            DiffInfoItem.HunkHeader.Action.DiscardHunk ->
                                onEvent.invoke(
                                    RepositoryViewEvent.Diff.OnDiscardHunk(
                                        hunkId = item.id,
                                    ),
                                )
                            DiffInfoItem.HunkHeader.Action.DiscardLines -> Unit
                            DiffInfoItem.HunkHeader.Action.StageLines -> Unit
                            DiffInfoItem.HunkHeader.Action.UnstageLines -> Unit
                            DiffInfoItem.HunkHeader.Action.ReverseHunk -> Unit
                            DiffInfoItem.HunkHeader.Action.ReverseLines -> Unit
                        }
                    },
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HunkHeader(
    item: DiffInfoItem.HunkHeader,
    onActionClick: (DiffInfoItem.HunkHeader.Action) -> Unit,
) =
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LocalDiffTheme.current.colors.header1Color)
            // TODO: magic numbers
            .padding(start = (48 + 4).dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ListTextView(
            text = item.text,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                Actions(item.actions, onActionClick)
            }
        }
    }

@Composable
private fun Actions(
    actions: ImmutableList<DiffInfoItem.HunkHeader.Action>,
    onActionClick: (DiffInfoItem.HunkHeader.Action) -> Unit,
) =
    actions.forEach { action ->
        DiffTextButton(
            text = when (action) {
                DiffInfoItem.HunkHeader.Action.StageHunk -> MR.strings.stage_hunk
                DiffInfoItem.HunkHeader.Action.UnstageHunk -> MR.strings.unstage_hunk
                DiffInfoItem.HunkHeader.Action.DiscardHunk -> MR.strings.discard_hunk
                DiffInfoItem.HunkHeader.Action.DiscardLines -> MR.strings.discard_lines
                DiffInfoItem.HunkHeader.Action.StageLines -> MR.strings.stage_lines
                DiffInfoItem.HunkHeader.Action.UnstageLines -> MR.strings.unstage_lines
                DiffInfoItem.HunkHeader.Action.ReverseHunk -> MR.strings.reverse_hunk
                DiffInfoItem.HunkHeader.Action.ReverseLines -> MR.strings.reverse_lines
            }.localized(),
            onClick = { onActionClick.invoke(action) },
        )
    }

@Preview
@Composable
private fun HunkHeaderPreview() {
    DiffTheme(ApplicationTheme.Dark) {
        HunkHeader(
            onActionClick = {},
            item = DiffInfoItem.HunkHeader(
                id = DiffInfoItem.Id.Hunk(0),
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
