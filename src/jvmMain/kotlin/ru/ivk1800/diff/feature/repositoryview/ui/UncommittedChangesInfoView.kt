package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ru.ivk1800.diff.compose.LocalDiffTheme
import ru.ivk1800.diff.feature.repositoryview.presentation.UncommittedChangesState
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem

@Composable
fun UncommittedChangesInfoView(
    modifier: Modifier = Modifier,
    state: UncommittedChangesState,
) =
    DraggableTwoPanes(
        orientation = Orientation.Vertical,
        percent = 50F,
    ) {
        when (state) {
            is UncommittedChangesState.Content -> {
                FilesPane(
                    modifier = modifier,
                    title = "Staged files",
                    files = state.staged,
                )
                FilesPane(
                    modifier = modifier,
                    title = "Unstaged files",
                    files = state.unstaged,
                )
            }

            UncommittedChangesState.None -> Box(modifier)
        }
    }

@Composable
private fun FilesPane(
    modifier: Modifier = Modifier,
    title: String,
    files: ImmutableList<CommitFileItem>,
) =
    Column(modifier = modifier.fillMaxSize()) {
        ListTextView(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalDiffTheme.current.colors.header1Color)
                .padding(8.dp),
            text = title,
        )
        Box(modifier = Modifier.fillMaxSize()) {
            val lazyListState = rememberLazyListState()
            CommitFilesListView(
                state = lazyListState,
                items = files,
                onSelected = { },
            )

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = lazyListState
                )
            )
        }
    }
