package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toImmutableSet
import ru.ivk1800.diff.feature.repositoryview.presentation.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FilesInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.HistoryState

@Composable
fun HistoryView(
    modifier: Modifier = Modifier,
    state: HistoryState,
    onEvent: (value: HistoryEvent) -> Unit,
) =
    Column(modifier = modifier) {
        TopSections()
        DraggableTwoPanes(
            orientation = Orientation.Vertical,
            percent = 50F,
        ) {
            CommitsTableView(
                modifier = Modifier.fillMaxSize(),
                state = state.commitsTableState,
                onItemsSelected = { items ->
                    if (items.size == 1 && items.first() is CommitTableItem.Id.UncommittedChanges) {
                        onEvent.invoke(HistoryEvent.OnUncommittedChangesSelected)
                    } else {
                        val ids = items.filterIsInstance<CommitTableItem.Id.Commit>().toImmutableSet()
                        onEvent.invoke(HistoryEvent.OnCommitsSelected(ids))
                    }
                },
                onLoadMore = { onEvent.invoke(HistoryEvent.OnLoadMoreCommits) }
            )
            BottomHorizontalPanes(state, onEvent)
        }
    }


@Composable
private fun BottomHorizontalPanes(
    state: HistoryState,
    onEvent: (value: HistoryEvent) -> Unit,
) =
    DraggableTwoPanes(
        orientation = Orientation.Horizontal,
        percent = 40F
    ) {
        when (state.filesInfoState) {
            is FilesInfoState.Commit -> CommitInfoPage(state.filesInfoState, onEvent)

            is FilesInfoState.UncommittedChanges -> UncommittedChangesInfoPane(state.filesInfoState, onEvent)

            FilesInfoState.None -> Box(
                modifier = Modifier.fillMaxSize(),
            )
        }
        DiffInfoView(
            onEvent = onEvent,
            modifier = Modifier.fillMaxSize(),
            state = state.diffInfoState,
        )
    }

@Composable
private fun UncommittedChangesInfoPane(
    filesInfoState: FilesInfoState.UncommittedChanges,
    onEvent: (value: HistoryEvent.UncommittedChanges) -> Unit,
) =
    UncommittedChangesInfoView(
        modifier = Modifier.fillMaxSize(),
        state = filesInfoState.state,
        onEvent = onEvent,
    )

@Composable
private fun CommitInfoPage(
    filesInfoState: FilesInfoState.Commit,
    onEvent: (value: HistoryEvent) -> Unit
) =
    CommitInfoView(
        modifier = Modifier.fillMaxSize(),
        state = filesInfoState.state,
        onFilesSelected = { event ->
            onEvent.invoke(HistoryEvent.OnFilesSelected(event))
        }
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
        SectionText(
            modifier = Modifier.weight(0.5F),
            text = "Commit",
        )
        SectionDivider()
        val commonModifier = Modifier.weight(1F)
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
