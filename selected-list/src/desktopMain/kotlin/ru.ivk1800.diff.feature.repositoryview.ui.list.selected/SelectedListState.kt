package ru.ivk1800.diff.feature.repositoryview.ui.list.selected

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class SelectedListState<Id>(
    onSelected: (items: Set<Id>) -> Unit,
    onInteractable: (item: Id) -> Boolean,
    onTestSelected: (items: Set<Id>) -> Boolean,
    calculateId: (index: Int) -> Id,
    calculateIndex: (id: Id) -> Int,
) {
    internal var selected by mutableStateOf(emptySet<Id>())
    internal var initialSelectedIndex by mutableStateOf(-1)

    internal val onSelected by mutableStateOf(onSelected)

    internal val onSelect by mutableStateOf(onTestSelected)

    internal val onInteractable by mutableStateOf(onInteractable)

    internal val calculateId by mutableStateOf(calculateId)

    private val calculateIndex by mutableStateOf(calculateIndex)

    fun select(value: Set<Id>) {
        selected = value
        initialSelectedIndex = if (value.isEmpty()) {
            -1
        } else {
            calculateIndex.invoke(selected.last())
        }
    }
}

