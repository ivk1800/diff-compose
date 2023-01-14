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
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEvent

@Composable
fun AppBar(onEvent: (value: RepositoryViewEvent) -> Unit) =
    TopAppBar(
        title = { },
        actions = {
            Button(
                onClick = { onEvent.invoke(RepositoryViewEvent.OpenFinder) }
            ) { Text("Show in Finder") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { onEvent.invoke(RepositoryViewEvent.OpenTerminal) }
            ) { Text("Terminal") }
            Spacer(modifier = Modifier.width(4.dp))
        },
        backgroundColor = MaterialTheme.colors.surface
    )
