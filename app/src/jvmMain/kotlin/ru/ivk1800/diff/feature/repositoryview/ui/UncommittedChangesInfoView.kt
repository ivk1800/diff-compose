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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.MR
import ru.ivk1800.diff.compose.LocalDiffTheme
import ru.ivk1800.diff.feature.repositoryview.presentation.event.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.UncommittedChangesState
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.rememberSelectedListState
import ru.ivk1800.diff.ui.compose.onKeyDownEvent

@Composable
fun UncommittedChangesInfoView(
    modifier: Modifier = Modifier,
    state: UncommittedChangesState,
    onEvent: (value: HistoryEvent.UncommittedChanges) -> Unit,
) =
    when (state) {
        is UncommittedChangesState.Content -> Content(
            modifier = modifier
                .focusable(),
            state,
            onEvent,
        )

        UncommittedChangesState.None -> Empty(modifier)
    }

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    state: UncommittedChangesState.Content,
    onEvent: (value: HistoryEvent.UncommittedChanges) -> Unit,
) =
    DraggableTwoPanes(
        modifier = modifier
            .background(MaterialTheme.colors.surface)
            .focusable(),
        orientation = Orientation.Vertical,
        percent = 50F,
    ) {
        StagedFilesPane(onEvent, state.staged)
        UnstagedFilesPane(onEvent, state.unstaged)
    }

@Composable
private fun Empty(
    modifier: Modifier = Modifier,
) =
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
    )

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UnstagedFilesPane(
    onEvent: (value: HistoryEvent.UncommittedChanges) -> Unit,
    state: UncommittedChangesState.Content.Unstaged,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    FilesPane(
        modifier = Modifier.onKeyDownEvent(key = Key.Spacebar) {
            onEvent.invoke(
                HistoryEvent.UncommittedChanges.OnAddFilesToStaged(
                    ids = state.selected,
                )
            )
        },
        title = MR.strings.unstaged_files.localized(),
        processAddText = MR.strings.stage_all.localized(),
        processSelectedText = MR.strings.stage_selected.localized(),
        files = state.files,
        selected = state.selected,
        vcsProcess = state.vcsProcess,
        onSelected = { files ->
            currentOnEvent.invoke(HistoryEvent.UncommittedChanges.OnUnstatedFilesSelected(files))
        },
        onStageAll = {
            onEvent.invoke(HistoryEvent.UncommittedChanges.OnAddAllToStaged)
        },
        onStageSelected = {
            onEvent.invoke(HistoryEvent.UncommittedChanges.OnAddFilesToStaged(state.selected))
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun StagedFilesPane(
    onEvent: (value: HistoryEvent.UncommittedChanges) -> Unit,
    state: UncommittedChangesState.Content.Staged,
) {
    val currentOnEvent by rememberUpdatedState(onEvent)
    FilesPane(
        modifier = Modifier.onKeyDownEvent(key = Key.Spacebar) {
            onEvent.invoke(
                HistoryEvent.UncommittedChanges.OnRemoveFilesFromStaged(
                    ids = state.selected,
                )
            )
        },
        title = MR.strings.staged_files.localized(),
        processAddText = MR.strings.unstage_all.localized(),
        processSelectedText = MR.strings.unstage_selected.localized(),
        files = state.files,
        selected = state.selected,
        vcsProcess = state.vcsProcess,
        onSelected = { files ->
            currentOnEvent.invoke(HistoryEvent.UncommittedChanges.OnStatedFilesSelected(files))
        },
        onStageAll = {
            onEvent.invoke(HistoryEvent.UncommittedChanges.OnRemoveAllFromStaged)
        },
        onStageSelected = {
            onEvent.invoke(HistoryEvent.UncommittedChanges.OnRemoveFilesFromStaged(state.selected))
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FilesPane(
    modifier: Modifier = Modifier,
    title: String,
    processAddText: String,
    processSelectedText: String,
    vcsProcess: Boolean,
    files: ImmutableList<CommitFileItem>,
    selected: ImmutableSet<CommitFileId>,
    onStageAll: () -> Unit,
    onStageSelected: () -> Unit,
    onSelected: (items: ImmutableSet<CommitFileId>) -> Unit,
) =
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalDiffTheme.current.colors.header1Color)
                .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            ListTextView(
                text = title,
            )
            Spacer(modifier = Modifier.weight(1F))
            CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                DiffTextButton(
                    enabled = files.isNotEmpty() && !vcsProcess,
                    onClick = onStageAll,
                    text = processAddText,
                )
                Spacer(modifier = Modifier.width(8.dp))
                DiffTextButton(
                    enabled = selected.isNotEmpty(),
                    onClick = onStageSelected,
                    text = processSelectedText,
                )
            }
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
