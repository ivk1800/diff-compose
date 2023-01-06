package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.MR
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitsTableState
import ru.ivk1800.diff.feature.repositoryview.presentation.FilesInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewState
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.SelectedList
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.rememberSelectedListState

@Composable
fun RepositoryView(
    state: RepositoryViewState,
    onEvent: (value: RepositoryViewEvent) -> Unit,
) =
    Scaffold(
        topBar = { AppBar(onEvent) }
    ) {
        Column {
            TopSections()
            DraggableTwoPanes(
                orientation = Orientation.Vertical,
                percent = 50F,
            ) {
                CommitsTable(
                    modifier = Modifier.fillMaxSize(),
                    state = state.commitsTableState,
                    onItemsSelected = { items ->
                        if (items.size == 1 && items.first() is CommitTableItem.Id.UncommittedChanges) {
                            onEvent.invoke(RepositoryViewEvent.OnUncommittedChangesSelected)
                        } else {
                            val ids = items.filterIsInstance<CommitTableItem.Id.Commit>().toImmutableSet()
                            onEvent.invoke(RepositoryViewEvent.OnCommitsSelected(ids))
                        }
                    },
                    onLoadMore = { onEvent.invoke(RepositoryViewEvent.OnLoadMoreCommits) }
                )
                BottomHorizontalPanes(state, onEvent)
            }
        }
    }

@Composable
private fun BottomHorizontalPanes(
    state: RepositoryViewState,
    onEvent: (value: RepositoryViewEvent) -> Unit,
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
    onEvent: (value: RepositoryViewEvent.UncommittedChanges) -> Unit,
) =
    UncommittedChangesInfoView(
        modifier = Modifier.fillMaxSize(),
        state = filesInfoState.state,
        onEvent = onEvent,
    )

@Composable
private fun CommitInfoPage(
    filesInfoState: FilesInfoState.Commit,
    onEvent: (value: RepositoryViewEvent) -> Unit
) =
    CommitInfoView(
        modifier = Modifier.fillMaxSize(),
        state = filesInfoState.state,
        onFilesSelected = { event ->
            onEvent.invoke(RepositoryViewEvent.OnFilesSelected(event))
        }
    )

@Composable
private fun AppBar(onEvent: (value: RepositoryViewEvent) -> Unit) =
    TopAppBar(
        title = { },
        actions = {
            Button(
                onClick = { onEvent.invoke(RepositoryViewEvent.OpenFinder) }
            ) { Text("Show in Finder") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onEvent.invoke(RepositoryViewEvent.OpenTerminal) }
            ) { Text("Terminal") }
            Spacer(modifier = Modifier.width(4.dp))
        },
        backgroundColor = MaterialTheme.colors.surface
    )

@Composable
private fun CommitsTable(
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
    modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)
) {
    ListTextView(
        modifier = Modifier
            .weight(3F),
        text = item.description,
    )
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
