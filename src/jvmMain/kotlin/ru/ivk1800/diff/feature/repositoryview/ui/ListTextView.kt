package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

@Composable
fun ListTextView(
    modifier: Modifier = Modifier,
    text: String,
) = Text(
    modifier = modifier,
    text = text,
    style = MaterialTheme.typography.caption,
    fontSize = 12.sp,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
)
