package ru.ivk1800.diff.feature.repositoryview.ui.list.selected

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun <Id> rememberSelectedListState(
    onSelected: (items: Set<Id>) -> Unit,
    calculateId: (index: Int) -> Id,
    calculateIndex: (id: Id) -> Int,
): SelectedListState<Id> = remember {
    SelectedListState(onSelected, calculateId, calculateIndex)
}
