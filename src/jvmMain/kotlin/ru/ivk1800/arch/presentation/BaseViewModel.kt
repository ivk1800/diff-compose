package ru.ivk1800.arch.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class BaseViewModel {
    // TODO: add main dispatcher
    protected val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob())

    open fun dispose() {
        viewModelScope.cancel()
    }
}