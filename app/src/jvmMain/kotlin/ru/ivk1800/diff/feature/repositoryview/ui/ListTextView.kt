package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@Composable
fun ListTextView(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
) = Text(
    modifier = modifier,
    text = text,
    color = color,
    style = MaterialTheme.typography.caption,
    fontSize = 12.sp,
    fontWeight = fontWeight,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
)
