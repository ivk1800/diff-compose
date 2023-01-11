package ru.ivk1800.diff.ext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
