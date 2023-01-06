package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitDescription

@Composable
fun CommitDescriptionView(
    modifier: Modifier = Modifier,
    description: CommitDescription
) = Box(modifier = modifier.padding(12.dp)) {
    Column {
        SelectionContainer {
            Text(
                text = description.message,
                style = MaterialTheme.typography.caption,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            KeysColumns()
            Spacer(modifier = Modifier.width(12.dp))
            ValuesColumn(description)
        }
    }
}

@Composable
private fun ValuesColumn(description: CommitDescription) =
    Column(
        horizontalAlignment = Alignment.Start,
    ) {
        val style = MaterialTheme.typography.caption
        SelectionContainer {
            Text(
                text = description.commit,
                style = style,
            )
        }
        SelectionContainer {
            Text(
                text = description.parents,
                style = style,
            )
        }
        SelectionContainer {
            Text(
                text = description.author,
                style = style,
            )
        }
        SelectionContainer {
            Text(
                text = description.date,
                style = style,
            )
        }
    }

@Composable
private fun KeysColumns() =
    CompositionLocalProvider(LocalContentAlpha provides 0.5F) {
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            val style = MaterialTheme.typography.caption
            Text(
                text = "Commit:",
                style = style,
            )
            Text(
                text = "Parents:",
                style = style,
            )
            Text(
                text = "Author:",
                style = style,
            )
            Text(
                text = "Date:",
                style = style,
            )
        }
    }
