package ru.ivk1800.diff.feature.repositoryview.ui.list.selected

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalWindowInfo
import kotlin.math.max
import kotlin.math.min

@Composable
fun <Id> SelectedList(
    modifier: Modifier = Modifier,
    state: SelectedListState<Id>,
    lazyListState: LazyListState = rememberLazyListState(),
    itemsCount: Int,
    itemContent: @Composable (index: Int) -> Unit,
) = SelectedListInternal<Id>(
    modifier = modifier,
    state = state,
    lazyListState = lazyListState,
    itemsCount = itemsCount,
    itemContent = itemContent,
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun <Id> SelectedListInternal(
    modifier: Modifier,
    state: SelectedListState<Id>,
    lazyListState: LazyListState,
    itemsCount: Int,
    itemContent: (@Composable (index: Int) -> Unit),
) {
    val windowInfo = LocalWindowInfo.current
    var hovered by remember { mutableStateOf(-1) }
    val colors = MaterialTheme.colors
    val hoveredColor by remember { mutableStateOf(colors.onSurface.copy(alpha = 0.3f)) }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
    ) {
        items(itemsCount) { index ->
            val color by derivedStateOf {
                if (hovered == index || state.selected.contains(state.calculateId.invoke(index))) {
                    hoveredColor
                } else {
                    Color.Transparent
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawRect(color)
                    }
                    .clickable {
                        if (windowInfo.keyboardModifiers.isShiftPressed && state.initialSelectedIndex != -1) {
                            if (state.selected.isEmpty()) {
                                val selectedId = state.calculateId.invoke(index)
                                if (state.selected.contains(selectedId)) {
                                    state.selected = state.selected - selectedId
                                } else {
                                    state.selected = state.selected + selectedId
                                }
                                state.onSelected.invoke(state.selected)
                            } else {
                                val selected = IntRange(
                                    min(index, state.initialSelectedIndex),
                                    max(index, state.initialSelectedIndex),
                                )
                                state.selected += selected.map(state.calculateId::invoke)
                                state.onSelected.invoke(state.selected)
                            }
                        } else if (windowInfo.keyboardModifiers.isMetaPressed) {
                            val selectedId = state.calculateId.invoke(index)
                            if (state.selected.contains(selectedId)) {
                                state.selected = state.selected - selectedId
                            } else {
                                state.selected = state.selected + selectedId
                            }
                            state.initialSelectedIndex = index
                            state.onSelected.invoke(state.selected)
                        } else {
                            val selectedId = state.calculateId.invoke(index)

                            if (state.selected.size == 1) {
                                if (state.selected.contains(selectedId)) {
                                    state.selected = emptySet()
                                    state.initialSelectedIndex = -1
                                    state.onSelected.invoke(state.selected)
                                } else {
                                    state.selected = setOf(selectedId)
                                    state.initialSelectedIndex = index
                                    state.onSelected.invoke(state.selected)
                                }
                            } else {
                                state.selected = setOf(selectedId)
                                state.initialSelectedIndex = index
                                state.onSelected.invoke(state.selected)
                            }
                        }
                    }
                    .onPointerEvent(PointerEventType.Exit) {
                        if (hovered == index) {
                            hovered = -1
                        }
                    }
                    .onPointerEvent(PointerEventType.Enter) {
                        hovered = index
                    },
            ) {
                itemContent.invoke(index)
            }
        }
    }
}
