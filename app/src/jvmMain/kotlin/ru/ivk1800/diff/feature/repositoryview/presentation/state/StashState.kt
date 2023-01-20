package ru.ivk1800.diff.feature.repositoryview.presentation.state

import kotlinx.collections.immutable.ImmutableList
import ru.ivk1800.diff.feature.repositoryview.presentation.model.StashItem

data class StashState(
    val stashes: ImmutableList<StashItem>,
)
