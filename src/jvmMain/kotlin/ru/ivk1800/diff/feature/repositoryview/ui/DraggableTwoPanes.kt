package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableTwoPanes(
    percent: Float,
    content: @Composable () -> Unit,
) = BoxWithConstraints {
    val dividerSize = 8.dp
    val dividerSizePx = with(LocalDensity.current) { dividerSize.toPx() }

    var dividerOffset by remember(percent) {
        mutableStateOf(
            IntOffset(
                x = 0,
                y = ((constraints.maxHeight * percent / 100) - (dividerSizePx / 2)).toInt()
            ),
        )
    }

    val targetPercent: Float by remember(key1 = constraints.maxHeight, key2 = dividerOffset) {
        val height = constraints.maxHeight
        val y = dividerOffset.y + dividerSizePx / 2
        mutableStateOf((y * 100 / height).coerceIn(0F, 100F))
    }

    Containers(
        percent = targetPercent,
        content = content,
    )
    Box(
        modifier = Modifier
            .height(dividerSize)
            .offset { dividerOffset }
            .fillMaxWidth()
            // TODO: fix draggable area
            .onDrag {
                dividerOffset += dividerOffset.copy(y = it.y.roundToInt())
            },
        contentAlignment = Alignment.Center,
    ) {
        Divider()
    }
}

@Composable
private fun Containers(
    percent: Float,
    content: @Composable () -> Unit
) = Layout(content) { measurables, constraints ->
    require(percent in 0F..100F)

    val firstHeight: Int = (constraints.maxHeight * percent / 100).roundToInt()
    val secondHeight = abs(constraints.maxHeight - firstHeight)

    check(measurables.size == 2)
    val placeables = measurables.mapIndexed { index, measurable ->
        val maxHeight = if (index == 0) {
            firstHeight
        } else {
            secondHeight
        }

        measurable.measure(constraints.copy(maxHeight = maxHeight))
    }

    var yPos = 0

    layout(constraints.maxWidth, constraints.maxHeight) {
        placeables.forEach { placeable ->
            placeable.placeRelative(0, yPos)
            yPos = placeable.height
        }
    }
}
