package ru.ivk1800.diff.feature.repositoryview.presentation.state.composer

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.model.StashItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.StashState

class StashStateComposer {
    fun getState(scope: CoroutineScope): StateFlow<StashState> =
        flowOf(
            StashState(
                stashes = List(30) {
                    StashItem(StashItem.Id(it), "Test $it")
                }.toImmutableList(),
            )
        )
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = getDefaultState(),
            )

    fun getDefaultState(): StashState =
        StashState(
            stashes = persistentListOf(),
        )
}
