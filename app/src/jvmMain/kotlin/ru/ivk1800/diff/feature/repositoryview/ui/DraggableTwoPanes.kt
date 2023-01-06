package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.awt.Cursor
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableTwoPanes(
    modifier: Modifier = Modifier,
    orientation: Orientation,
    percent: Float,
    content: @Composable () -> Unit,
) = BoxWithConstraints(
    modifier = modifier,
) {
    require(percent in 0F..100F)
    val dividerSize = 8.dp
    val dividerSizePx = with(LocalDensity.current) { dividerSize.toPx() }

    val targetSide = when (orientation) {
        Orientation.Vertical -> constraints.maxHeight
        Orientation.Horizontal -> constraints.maxWidth
    }

    var dividerOffset by remember(percent) {
        mutableStateOf(
            when (orientation) {
                Orientation.Vertical -> IntOffset(
                    x = 0,
                    y = ((targetSide * percent / 100) - (dividerSizePx / 2)).toInt()
                )

                Orientation.Horizontal -> IntOffset(
                    x = ((targetSide * percent / 100) - (dividerSizePx / 2)).toInt(),
                    y = 0,
                )
            }
        )
    }

    val targetPercent: Float by remember(key1 = targetSide, key2 = dividerOffset) {
        val targetAxis = when (orientation) {
            Orientation.Vertical -> dividerOffset.y
            Orientation.Horizontal -> dividerOffset.x
        } + dividerSizePx / 2
        mutableStateOf((targetAxis * 100 / targetSide).coerceIn(0F, 100F))
    }

    Containers(
        orientation = orientation,
        percent = targetPercent,
        content = content,
    )

    val dividerMod = when (orientation) {
        Orientation.Vertical -> Modifier.height(dividerSize)
            .fillMaxWidth()

        Orientation.Horizontal -> Modifier.width(dividerSize)
            .fillMaxHeight()
    }

    DraggableDivider(
        modifier = Modifier
            .offset { dividerOffset }
            .then(dividerMod)
            // TODO: fix draggable area
            .onDrag {
                dividerOffset += when (orientation) {
                    Orientation.Vertical -> dividerOffset.copy(y = it.y.roundToInt())
                    Orientation.Horizontal -> dividerOffset.copy(x = it.x.roundToInt())
                }
            }
            .dividerCursor(orientation),
        orientation = orientation,
    )
}

@Composable
private fun DraggableDivider(
    modifier: Modifier = Modifier,
    orientation: Orientation,
) = Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
) {
    Box(
        modifier = when (orientation) {
            Orientation.Vertical -> Modifier.height(1.dp)
                .fillMaxWidth()

            Orientation.Horizontal -> Modifier.width(1.dp)
                .fillMaxHeight()
        }
            .background(color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
    )
}

private fun Modifier.dividerCursor(orientation: Orientation): Modifier =
    pointerHoverIcon(
        PointerIcon(
            Cursor(
                when (orientation) {
                    Orientation.Vertical -> Cursor.S_RESIZE_CURSOR
                    Orientation.Horizontal -> Cursor.E_RESIZE_CURSOR
                }
            )
        )
    )

@Composable
private fun Containers(
    orientation: Orientation,
    percent: Float,
    content: @Composable () -> Unit
) = Layout(content) { measurables, constraints ->
    require(measurables.size == 2)

    val placeables = when (orientation) {
        Orientation.Vertical -> verticalMeasure(percent, measurables, constraints)
        Orientation.Horizontal -> horizontalMeasure(percent, measurables, constraints)
    }

    var targetAxis = 0

    layout(constraints.maxWidth, constraints.maxHeight) {
        placeables.forEach { placeable ->
            targetAxis = when (orientation) {
                Orientation.Vertical -> {
                    placeable.placeRelative(0, targetAxis)
                    placeable.height
                }

                Orientation.Horizontal -> {
                    placeable.placeRelative(targetAxis, 0)
                    placeable.width
                }
            }
        }
    }
}

private fun verticalMeasure(percent: Float, measurables: List<Measurable>, constraints: Constraints): List<Placeable> {
    val firstHeight: Int = (constraints.maxHeight * percent / 100).roundToInt()
    val secondHeight = abs(constraints.maxHeight - firstHeight)

    check(measurables.size == 2)
    return measurables.mapIndexed { index, measurable ->
        val maxHeight = if (index == 0) {
            firstHeight
        } else {
            secondHeight
        }
        measurable.measure(constraints.copy(maxHeight = maxHeight))
    }
}

private fun horizontalMeasure(
    percent: Float,
    measurables: List<Measurable>,
    constraints: Constraints
): List<Placeable> {
    val firstWidth: Int = (constraints.maxWidth * percent / 100).roundToInt()
    val secondWidth = abs(constraints.maxWidth - firstWidth)

    check(measurables.size == 2)
    return measurables.mapIndexed { index, measurable ->
        val maxWidth = if (index == 0) {
            firstWidth
        } else {
            secondWidth
        }
        measurable.measure(constraints.copy(maxWidth = maxWidth))
    }
}
