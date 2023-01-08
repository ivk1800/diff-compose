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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.MR
import ru.ivk1800.diff.compose.LocalDiffTheme
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.UncommittedChangesState
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.rememberSelectedListState
import ru.ivk1800.diff.ui.compose.onKeyDownEvent

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
        when (state) {
            is UncommittedChangesState.Content -> {
                StagedFilesPane(onEvent, state.staged)
                UnstagedFilesPane(onEvent, state.unstaged)
            }

            UncommittedChangesState.None -> Box(modifier)
        }
    }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UnstagedFilesPane(
    onEvent: (value: RepositoryViewEvent.UncommittedChanges) -> Unit,
    state: UncommittedChangesState.Content.Unstaged,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    val focusManager = LocalFocusManager.current
    FilesPane(
        modifier = Modifier.onKeyDownEvent(key = Key.Spacebar) {
            onEvent.invoke(
                RepositoryViewEvent.UncommittedChanges.OnAddFilesToStaged(
                    ids = state.selected,
                )
            )
        },
        title = MR.strings.unstaged_files.localized(),
        files = state.files,
        selected = state.selected,
        vcsProcess = state.vcsProcess,
        onSelected = { files ->
            currentOnEvent.invoke(RepositoryViewEvent.UncommittedChanges.OnUnstatedFilesSelected(files))
        },
        onStageActionClick = {
            focusManager.clearFocus()
            onEvent.invoke(RepositoryViewEvent.UncommittedChanges.OnAddAllToStaged)
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun StagedFilesPane(
    onEvent: (value: RepositoryViewEvent.UncommittedChanges) -> Unit,
    state: UncommittedChangesState.Content.Staged,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    val focusManager = LocalFocusManager.current
    FilesPane(
        modifier = Modifier.onKeyDownEvent(key = Key.Spacebar) {
            onEvent.invoke(
                RepositoryViewEvent.UncommittedChanges.OnRemoveFilesFromStaged(
                    ids = state.selected,
                )
            )
        },
        title = MR.strings.staged_files.localized(),
        files = state.files,
        selected = state.selected,
        vcsProcess = !state.vcsProcess,
        onSelected = { files ->
            currentOnEvent.invoke(RepositoryViewEvent.UncommittedChanges.OnStatedFilesSelected(files))
        },
        onStageActionClick = {
            focusManager.clearFocus()
            onEvent.invoke(RepositoryViewEvent.UncommittedChanges.OnRemoveAllFromStaged)
        },
    )
}

@Composable
private fun FilesPane(
    modifier: Modifier = Modifier,
    title: String,
    vcsProcess: Boolean,
    files: ImmutableList<CommitFileItem>,
    selected: ImmutableSet<CommitFileId>,
    onStageActionClick: () -> Unit,
    onSelected: (items: ImmutableSet<CommitFileId>) -> Unit,
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
            val currentSelected by rememberUpdatedState(selected)
            val lazyListState = rememberLazyListState()

            val selectableListState = rememberSelectedListState(
                onSelect = { selected ->
                    onSelected.invoke(selected.toImmutableSet())
                    false
                },
                calculateIndex = { itemId -> currentFiles.indexOfFirst { it.id == itemId } },
                calculateId = { index -> currentFiles[index].id },
            )

            LaunchedEffect(key1 = selectableListState) {
                snapshotFlow { currentSelected }
                    .onEach(selectableListState::select)
                    .launchIn(this)
            }

            CommitFilesListView(
                lazyListState = lazyListState,
                state = selectableListState,
                items = files,
            )

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = lazyListState,
                )
            )
        }
    }
