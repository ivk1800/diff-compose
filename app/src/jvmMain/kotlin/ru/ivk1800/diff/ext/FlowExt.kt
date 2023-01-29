package ru.ivk1800.diff.ext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.cancellation.CancellationException

fun <T> Flow<T>.onFirst(action: suspend (T) -> Unit): Flow<T> = flow {
    var called = false
    collect { value ->
        if (!called) {
            action.invoke(value)
            called = true
        }
        emit(value)
    }
}

fun <T> Flow<T>.catchContinue(action: suspend (Throwable) -> Flow<T>): Flow<T> = flow {
    try {
        collect { value ->
            emit(value)
        }
    } catch (e: Throwable) {
        if (e !is CancellationException) {
            action.invoke(e).collect { value ->
                emit(value)
            }
        }
    }
}

