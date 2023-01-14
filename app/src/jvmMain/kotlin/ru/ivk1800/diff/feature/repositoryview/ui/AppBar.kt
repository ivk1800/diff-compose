package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.ivk1800.diff.feature.repositoryview.presentation.HistoryEvent

@Composable
fun AppBar(onEvent: (value: HistoryEvent) -> Unit) =
    TopAppBar(
        title = { },
        actions = {
            Button(
                onClick = { onEvent.invoke(HistoryEvent.OpenFinder) }
            ) { Text("Show in Finder") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onEvent.invoke(HistoryEvent.OpenTerminal) }
            ) { Text("Terminal") }
            Spacer(modifier = Modifier.width(4.dp))
        },
        backgroundColor = MaterialTheme.colors.surface
    )
