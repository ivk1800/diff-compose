package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.MR
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitLabel
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitsTableState
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.SelectedList
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.rememberSelectedListState

@Composable
fun CommitsTableView(
    modifier: Modifier = Modifier,
    state: CommitsTableState,
    onItemsSelected: (items: Set<CommitTableItem.Id>) -> Unit,
    onLoadMore: () -> Unit,
) = Box(modifier = modifier) {
    when (state) {
        is CommitsTableState.Content -> Commits(
            state = state,
            onSelected = onItemsSelected,
            onLoadMore = onLoadMore,
        )

        CommitsTableState.Loading -> LazyColumn(
            userScrollEnabled = false,
        ) {
            items(Int.MAX_VALUE) {
                CommitItemView(
                    modifier = Modifier
                        .fillMaxWidth(),
                    item = CommitTableItem.Commit(
                        id = CommitId(""),
                        description = "...",
                        commit = "...",
                        author = "...",
                        date = "...",
                        labels = persistentListOf(),
                    ),
                )
            }
        }
    }
}

@Composable
private fun Commits(
    state: CommitsTableState.Content,
    onSelected: (items: Set<CommitTableItem.Id>) -> Unit,
    onLoadMore: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val currentOnSelected by rememberUpdatedState(onSelected)
    val currentSelected by rememberUpdatedState(state.selected)
    val currentCommits by rememberUpdatedState(state.commits)

    LaunchedEffect(key1 = null) {
        snapshotFlow { lazyListState.layoutInfo }
            .map { layoutInfo -> layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1 }
            .distinctUntilChanged()
            .onEach { isEnd ->
                if (isEnd) {
                    currentOnLoadMore.invoke()
                }
            }.launchIn(this)
    }

    val itemContent = remember<@Composable (index: Int) -> Unit> {
        { index ->
            when (val item = currentCommits[index]) {
                is CommitTableItem.Commit -> CommitItemView(
                    modifier = Modifier,
                    item = item,
                )

                CommitTableItem.UncommittedChanges -> UncommittedChangesItemView()
            }
        }
    }
    val selectableListState = rememberSelectedListState(
        calculateIndex = { itemId ->
            when (itemId) {
                is CommitTableItem.Id.Commit -> currentCommits
                    .filterIsInstance<CommitTableItem.Commit>()
                    .indexOfFirst { it.id == itemId.id }

                CommitTableItem.Id.UncommittedChanges -> currentCommits.indexOfFirst {
                    it is CommitTableItem.UncommittedChanges
                }
            }
        },
        calculateId = { index ->
            when (val item = currentCommits[index]) {
                is CommitTableItem.Commit -> CommitTableItem.Id.Commit(item.id)
                CommitTableItem.UncommittedChanges -> CommitTableItem.Id.UncommittedChanges
            }
        },
        onSelect = { selectedItems ->
            currentOnSelected.invoke(selectedItems)
            false
        }
    )

    LaunchedEffect(key1 = selectableListState) {
        snapshotFlow { currentSelected }
            .onEach(selectableListState::select)
            .launchIn(this)
    }

    SelectedList<CommitTableItem.Id>(
        state = selectableListState,
        lazyListState = lazyListState,
        itemsCount = state.commits.size,
        itemContent = itemContent,
    )
}

@Composable
private fun UncommittedChangesItemView() {
    ListTextView(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp),
        text = MR.strings.uncommitted_changes.localized(),
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun CommitItemView(
    modifier: Modifier = Modifier,
    item: CommitTableItem.Commit,
) = Row(
    modifier = modifier.padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    Row(
        modifier = Modifier.weight(3F),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Labels(item.labels)
        if (item.labels.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
        }
        ListTextView(
            modifier = Modifier.padding(vertical = 4.dp),
            text = item.description,
        )
    }
    ListTextView(
        modifier = Modifier
            .padding(start = 4.dp)
            .weight(0.5F),
        text = item.commit,
    )
    val commonModifier = Modifier
        .padding(start = 4.dp)
        .weight(1F)
    ListTextView(
        modifier = commonModifier,
        text = item.author,
    )
    ListTextView(
        modifier = commonModifier,
        text = item.date,
    )
}

@Composable
private fun Labels(labels: ImmutableList<CommitLabel>) =
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        labels.forEach { label ->
            Label(label)
        }
    }

@Composable
private fun Label(label: CommitLabel) =
    Box(
        modifier = Modifier.clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colors.primary)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        ListTextView(
            text = when (label) {
                is CommitLabel.Branch -> label.name
            },
            color = ButtonDefaults.buttonColors().contentColor(true).value,
        )
    }
