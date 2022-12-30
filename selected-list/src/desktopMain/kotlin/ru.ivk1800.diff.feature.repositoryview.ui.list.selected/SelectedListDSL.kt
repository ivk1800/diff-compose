package ru.ivk1800.diff.feature.repositoryview.ui.list.selected

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.collections.immutable.ImmutableSet

@Composable
fun <Id> rememberSelectedListState(
    onSelected: ((items: ImmutableSet<Id>) -> Unit)? = null,
    onSelect: (items: Set<Id>) -> Boolean = { true },
    onInteractable: (item: Id) -> Boolean = { true },
    calculateId: (index: Int) -> Id,
    calculateIndex: (id: Id) -> Int,
): SelectedListState<Id> = remember {
    SelectedListState(
        onSelected,
        onInteractable = onInteractable,
        onTestSelected = onSelect,
        calculateId,
        calculateIndex,
    )
}
