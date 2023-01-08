package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitDescription
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.rememberSelectedListState

@Composable
fun CommitInfoView(
    modifier: Modifier = Modifier,
    state: CommitInfoState,
    onFilesSelected: (items: ImmutableSet<CommitFileId>) -> Unit,
) = Box(modifier = modifier) {
    when (state) {
        is CommitInfoState.Content -> {
            DraggableTwoPanes(
                orientation = Orientation.Vertical,
                percent = 50F,
            ) {
                val currentSelected by rememberUpdatedState(state.selected)
                val currentFiles by rememberUpdatedState(state.files)
                val selectableListState = rememberSelectedListState<CommitFileId>(
                    onSelect = { files ->
                        onFilesSelected.invoke(files.toImmutableSet())
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
                    modifier = Modifier.fillMaxSize(),
                    items = state.files,
                    state = selectableListState
                )
                DescriptionPane(state.description)
            }
        }

        CommitInfoState.None -> Unit
    }
}

@Composable
private fun DescriptionPane(
    description: CommitDescription
) = Box(modifier = Modifier.fillMaxSize()) {
    val verticalState = rememberScrollState()
    val horizontalState = rememberScrollState()
    CommitDescriptionView(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(horizontalState)
            .verticalScroll(verticalState),
        description = description,
    )
    VerticalScrollbar(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight(),
        adapter = rememberScrollbarAdapter(verticalState)
    )
    HorizontalScrollbar(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth(),
        adapter = rememberScrollbarAdapter(horizontalState)
    )
}
