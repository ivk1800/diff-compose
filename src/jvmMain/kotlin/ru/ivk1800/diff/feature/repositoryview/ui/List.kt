package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
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
import androidx.compose.ui.input.pointer.isShiftPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalWindowInfo
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun List(
    modifier: Modifier = Modifier,
    itemsCount: Int,
    itemContent: @Composable LazyItemScope.(index: Int) -> Unit,
    onSelected: (event: SelectEvent) -> Unit = { },
    state: LazyListState = rememberLazyListState(),
) {
    val windowInfo = LocalWindowInfo.current

    var initialSelected by remember { mutableStateOf(-1) }
    var selected by remember { mutableStateOf(IntRange(-1, -1)) }
    var hovered by remember { mutableStateOf(-1) }

    val colors = MaterialTheme.colors
    val hoveredColor by remember { mutableStateOf(colors.onSurface.copy(alpha = 0.3f)) }

    LazyColumn(
        state = state,
        modifier = modifier,
    ) {
        items(itemsCount) { index ->
            val color by derivedStateOf {
                if (hovered == index || selected.contains(index)) {
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
                        if (windowInfo.keyboardModifiers.isShiftPressed && initialSelected != -1) {
                            selected = IntRange(min(index, initialSelected), max(index, initialSelected))
                            onSelected.invoke(SelectEvent.Selected(selected))
                        } else {
                            if (initialSelected != index) {
                                initialSelected = index
                                selected = IntRange(index, index)
                                onSelected.invoke(SelectEvent.Selected(selected))
                            } else {
                                initialSelected = -1
                                selected = IntRange(-1, -1)
                                onSelected.invoke(SelectEvent.Unselect)
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
                itemContent.invoke(this@items, index)
            }
        }
    }
}

sealed interface SelectEvent {
    data class Selected(val range: IntRange): SelectEvent
    object Unselect: SelectEvent
}
