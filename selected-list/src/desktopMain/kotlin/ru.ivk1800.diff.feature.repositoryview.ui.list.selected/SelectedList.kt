package ru.ivk1800.diff.feature.repositoryview.ui.list.selected

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.isMetaPressed
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlin.math.max
import kotlin.math.min

@Composable
fun <Id : Any> SelectedList(
    modifier: Modifier = Modifier,
    state: SelectedListState<Id>,
    lazyListState: LazyListState = rememberLazyListState(),
    itemsCount: Int,
    itemContent: @Composable (index: Int) -> Unit,
) = SelectedListWithScrollBarInternal(
    modifier = modifier,
    state = state,
    lazyListState = lazyListState,
    itemsCount = itemsCount,
    itemContent = itemContent,
)

@Composable
private fun <Id : Any> SelectedListWithScrollBarInternal(
    modifier: Modifier,
    state: SelectedListState<Id>,
    lazyListState: LazyListState,
    itemsCount: Int,
    itemContent: (@Composable (index: Int) -> Unit),
) =
    Box {
        SelectedListInternal(modifier, state, lazyListState, itemsCount, itemContent)

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = lazyListState,
            )
        )
    }

@Composable
private fun <Id : Any> SelectedListInternal(
    modifier: Modifier,
    state: SelectedListState<Id>,
    lazyListState: LazyListState,
    itemsCount: Int,
    itemContent: (@Composable (index: Int) -> Unit),
) {
    val windowInfo = LocalWindowInfo.current
    val colors = MaterialTheme.colors
    val hoveredColor by remember { mutableStateOf(colors.onSurface.copy(alpha = 0.3f)) }

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
    ) {
        items(
            itemsCount,
            key = state.calculateId::invoke,
        ) { index ->
            val color by derivedStateOf {
                if (state.selected.contains(state.calculateId.invoke(index))) {
                    hoveredColor
                } else {
                    Color.Transparent
                }
            }
            // TODO: remember?
            val interactable = state.onInteractable.invoke(state.calculateId.invoke(index))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        drawContent()
                        drawRect(color)
                    }
                    .clickable(
                        enabled = interactable,
                        indication = LocalIndication.current,
                        interactionSource = remember { MutableInteractionSource() },
                    ) {
                        if (windowInfo.keyboardModifiers.isShiftPressed && state.initialSelectedIndex != -1) {
                            if (state.selected.isEmpty()) {
                                val selectedId: Id = state.calculateId.invoke(index)

                                if (state.onSelect.invoke(persistentSetOf(selectedId))) {
                                    if (state.selected.contains(selectedId)) {
                                        state.selected = (state.selected - selectedId).toImmutableSet()
                                    } else {
                                        state.selected = (state.selected + selectedId).toImmutableSet()
                                    }
                                    state.onSelected?.invoke(state.selected)
                                }
                            } else {
                                val selected = IntRange(
                                    min(index, state.initialSelectedIndex),
                                    max(index, state.initialSelectedIndex),
                                )
                                val newSelected = selected.map(state.calculateId::invoke).toImmutableSet()
                                if (state.onSelect.invoke(newSelected)) {
                                    state.selected = (newSelected + state.selected).toImmutableSet()
                                    state.onSelected?.invoke(state.selected)
                                }
                            }
                        } else if (windowInfo.keyboardModifiers.isMetaPressed) {
                            val selectedId = state.calculateId.invoke(index)
                            if (state.onSelect.invoke(persistentSetOf(selectedId))) {
                                if (state.selected.contains(selectedId)) {
                                    state.selected = (state.selected - selectedId).toImmutableSet()
                                } else {
                                    state.selected = (state.selected + selectedId).toImmutableSet()
                                }
                                state.initialSelectedIndex = index
                                state.onSelected?.invoke(state.selected)
                            }
                        } else {
                            val selectedId = state.calculateId.invoke(index)

                            if (state.selected.size == 1) {
                                if (state.selected.contains(selectedId)) {
                                    val selected = persistentSetOf<Id>()
                                    if (state.onSelect.invoke(selected)) {
                                        state.selected = selected
                                        state.initialSelectedIndex = -1
                                        state.onSelected?.invoke(state.selected)
                                    }
                                } else {
                                    val selected = persistentSetOf(selectedId)
                                    if (state.onSelect.invoke(selected)) {
                                        state.selected = selected
                                        state.initialSelectedIndex = index
                                        state.onSelected?.invoke(state.selected)
                                    }
                                }
                            } else {
                                val selected = persistentSetOf(selectedId)
                                if (state.onSelect.invoke(selected)) {
                                    state.selected = persistentSetOf(selectedId)
                                    state.initialSelectedIndex = index
                                    state.onSelected?.invoke(state.selected)
                                }
                            }
                        }
                    },
            ) {
                itemContent.invoke(index)
            }
        }
    }
}
