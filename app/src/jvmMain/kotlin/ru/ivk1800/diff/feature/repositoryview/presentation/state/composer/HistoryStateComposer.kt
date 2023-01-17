package ru.ivk1800.diff.feature.repositoryview.presentation.state.composer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitsTableManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.DiffInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.FilesInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitsTableState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.DiffInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FilesInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.HistoryState

class HistoryStateComposer(
    private val filesInfoManager: FilesInfoManager,
    private val commitsTableManager: CommitsTableManager,
    private val diffInfoManager: DiffInfoManager,
) {
    fun getState(scope: CoroutineScope): StateFlow<HistoryState> =
        combine(
            filesInfoManager.state,
            commitsTableManager.state,
            diffInfoManager.state,
        ) { activeState, commitsTableState, diffInfoState ->
            HistoryState(
                commitsTableState = commitsTableState,
                diffInfoState = diffInfoState,
                filesInfoState = activeState,
            )
        }
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = getDefaultState(),
            )

    fun getDefaultState(): HistoryState =
        HistoryState(
            commitsTableState = CommitsTableState.Loading,
            diffInfoState = DiffInfoState.None,
            filesInfoState = FilesInfoState.None,
        )
}
