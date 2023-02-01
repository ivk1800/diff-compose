package ru.ivk1800.diff.feature.repositoryview.presentation.state.composer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommandsActivityState
import ru.ivk1800.vcs.logged.LoggedVcs

class CommandsActivityStateComposer(
    private val loggedVcs: LoggedVcs,
) {
    fun getState(scope: CoroutineScope): StateFlow<CommandsActivityState> =
        loggedVcs.activeCommands
            .map { activeCommands ->
                delay(200)
                CommandsActivityState(activeCommands.takeIf { it.isNotEmpty() }?.joinToString { it.name })
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.Lazily,
                initialValue = getDefaultState(),
            )

    fun getDefaultState() = CommandsActivityState(activeCommand = null)
}
