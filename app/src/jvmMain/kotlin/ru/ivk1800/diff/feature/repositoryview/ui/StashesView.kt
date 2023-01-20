package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.ivk1800.diff.feature.repositoryview.presentation.model.StashItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.StashState
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.SelectedList
import ru.ivk1800.diff.feature.repositoryview.ui.list.selected.rememberSelectedListState

@Composable
fun StashesView(
    modifier: Modifier = Modifier,
    state: StashState,
) =
    Box(modifier.background(MaterialTheme.colors.background)) {
        val currentItems by rememberUpdatedState(state.stashes)

        val selectableListState = rememberSelectedListState(
            calculateIndex = { itemId ->
                currentItems
                    .indexOfFirst { it.id == itemId }
            },
            calculateId = { index ->
                currentItems[index].id
            },
            onSelect = { false },
        )

        val itemContent = remember<@Composable (index: Int) -> Unit> {
            { index ->
                val item = currentItems[index]
                ListTextView(
                    modifier = Modifier.padding(4.dp),
                    text = item.message,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        SelectedList(
            state = selectableListState,
            itemsCount = state.stashes.size,
            itemContent = itemContent,
        )
    }
