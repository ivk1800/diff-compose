package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitItem
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewState
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem

@Composable
fun RepositoryView(
    state: RepositoryViewState,
    onEvent: (value: RepositoryViewEvent) -> Unit,
) {
    Scaffold(
        topBar = { AppBar(onEvent) }
    ) {
        Column {
            TopSections()
            CommitsTable(state)
            Divider()
            CommitFilesListView(
                modifier = Modifier.height(300.dp),
                items = remember {
                    persistentListOf(
                        CommitFileItem(
                            type = CommitFileItem.Type.Edited,
                            name = "app/main/src/Test1.kt",
                        ),
                        CommitFileItem(
                            type = CommitFileItem.Type.Moved,
                            name = "app/main/src/Test2.kt",
                        ),
                        CommitFileItem(
                            type = CommitFileItem.Type.Added,
                            name = "app/main/src/Tes3.kt",
                        ),
                    )
                }
            )
        }
    }
}

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
private fun CommitsTable(state: RepositoryViewState) =
    when (state) {
        is RepositoryViewState.Content -> Commits(state.commits)
        RepositoryViewState.Loading -> LazyColumn(
            userScrollEnabled = false,
        ) {
            items(Int.MAX_VALUE) {
                CommitItemView(
                    modifier = Modifier
                        .fillMaxWidth(),
                    item = CommitItem(
                        description = "...",
                        commit = "...",
                        author = "...",
                        date = "...",
                    ),
                )
            }
        }
    }

@Composable
private fun Commits(items: ImmutableList<CommitItem>) =
    List(
        itemsCount = items.size,
        itemContent = { index ->
            val item = items[index]
            CommitItemView(
                modifier = Modifier,
                item = item,
            )
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

@Composable
private fun CommitItemView(
    modifier: Modifier = Modifier,
    item: CommitItem,
) = Row(
    modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)
) {
    ListTextView(
        modifier = Modifier
            .padding(start = 4.dp)
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
