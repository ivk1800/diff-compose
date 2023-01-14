package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentSetOf
import ru.ivk1800.diff.MR
import ru.ivk1800.diff.feature.repositoryview.presentation.event.SidePanelEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.state.SidePanelState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.WorkspaceState
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.SelectedList
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.rememberSelectedListState

@Composable
fun SidePanel(
    modifier: Modifier = Modifier,
    state: SidePanelState,
    onEvent: (value: SidePanelEvent) -> Unit,
) =
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Workspace(
            state.workspaceState,
            onEvent,
        )
        Divider()
    }

@Composable
private fun Workspace(
    state: WorkspaceState,
    onEvent: (value: SidePanelEvent) -> Unit,
) =
    Column {
        val selectableListState = rememberSelectedListState(
            onSelect = { selectedItems ->
                if (selectedItems.size == 1) {
                    onEvent.invoke(SidePanelEvent.OnSectionUnselected(selectedItems.first()))
                }
                false
            },
            calculateIndex = { itemId ->
                when (itemId) {
                    WorkspaceState.Section.FileStatus -> 0
                    WorkspaceState.Section.History -> 1
                }
            },
            calculateId = { index ->
                when (index) {
                    0 -> WorkspaceState.Section.FileStatus
                    1 -> WorkspaceState.Section.History
                    else -> error("TODO")
                }
            },
        )

        LaunchedEffect(key1 = state.activeSection) {
            selectableListState.select(persistentSetOf(state.activeSection))
        }

        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            text = MR.strings.workspace_title.localized(),
        )
        SelectedList(
            state = selectableListState,
            itemsCount = 2,
            itemContent = { index ->
                Text(
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.button,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    text = when (index) {
                        0 -> MR.strings.workspace_file_status.localized()
                        1 -> MR.strings.workspace_history.localized()
                        else -> error("TODO")
                    }
                )
            },
        )
    }
