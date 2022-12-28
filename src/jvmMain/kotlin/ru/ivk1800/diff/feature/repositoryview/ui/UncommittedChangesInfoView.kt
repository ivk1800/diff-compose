package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import ru.ivk1800.diff.compose.LocalDiffTheme
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.UncommittedChangesState
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.rememberSelectedListState
import ru.ivk1800.diff.ui.compose.onKeyDownEvent

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UncommittedChangesInfoView(
    modifier: Modifier = Modifier,
    state: UncommittedChangesState,
    onEvent: (value: RepositoryViewEvent.UncommittedChanges) -> Unit,
) =
    DraggableTwoPanes(
        modifier = modifier
            .focusable(),
        orientation = Orientation.Vertical,
        percent = 50F,
    ) {
        val focusManager = LocalFocusManager.current
        when (state) {
            is UncommittedChangesState.Content -> {
                FilesPane(
                    modifier = Modifier.onKeyDownEvent(key = Key.Spacebar) {
                        onEvent.invoke(
                            RepositoryViewEvent.UncommittedChanges.OnRemoveFileFromStaged(
                                id = CommitFileId(""),
                            )
                        )
                    },
                    title = "Staged files",
                    files = state.staged.files,
                    vcsProcess = !state.staged.vcsProcess,
                    onStageActionClick = {
                        focusManager.clearFocus()
                        onEvent.invoke(RepositoryViewEvent.UncommittedChanges.OnRemoveAllFromStaged)
                    }
                )
                FilesPane(
                    modifier = Modifier.onKeyDownEvent(key = Key.Spacebar) {
                        onEvent.invoke(
                            RepositoryViewEvent.UncommittedChanges.OnAddFileToStaged(
                                id = CommitFileId(""),
                            )
                        )
                    },
                    title = "Unstaged files",
                    files = state.unstaged.files,
                    vcsProcess = state.unstaged.vcsProcess,
                    onStageActionClick = {
                        focusManager.clearFocus()
                        onEvent.invoke(RepositoryViewEvent.UncommittedChanges.OnAddAllToStaged)
                    }
                )
            }

            UncommittedChangesState.None -> Box(modifier)
        }
    }

@Composable
private fun FilesPane(
    modifier: Modifier = Modifier,
    title: String,
    vcsProcess: Boolean,
    files: ImmutableList<CommitFileItem>,
    onStageActionClick: () -> Unit,
) =
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalDiffTheme.current.colors.header1Color)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                modifier = Modifier.scale(0.7F)
                    .size(16.dp),
                checked = vcsProcess,
                onCheckedChange = {
                    onStageActionClick.invoke()
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colors.primary,
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            ListTextView(
                text = title,
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            val currentFiles by rememberUpdatedState(files)
            val lazyListState = rememberLazyListState()
            CommitFilesListView(
                lazyListState = lazyListState,
                state = rememberSelectedListState(
                    onSelected = {
                        println(it)
                    },
                    calculateIndex = { itemId -> currentFiles.indexOfFirst { it.id == itemId } },
                    calculateId = { index -> currentFiles[index].id },
                ),
                items = files,
            )

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = lazyListState
                )
            )
        }
    }
