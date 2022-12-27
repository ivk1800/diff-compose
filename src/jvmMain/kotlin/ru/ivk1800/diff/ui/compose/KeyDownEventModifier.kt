package ru.ivk1800.diff.ui.compose

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

fun Modifier.onKeyDownEvent(key: Key, onDown: () -> Unit): Modifier =
    composed {
        val actualOnDown by rememberUpdatedState(onDown)

        val keyEventChannel = remember { Channel<KeyEventType>() }
        val scope = rememberCoroutineScope()

        DisposableEffect(keyEventChannel) {
            onDispose {
                keyEventChannel.close()
            }
        }

        LaunchedEffect(keyEventChannel) {
            keyEventChannel.consumeAsFlow()
                .filter { it == KeyEventType.KeyUp || it == KeyEventType.KeyDown }
                .distinctUntilChanged()
                .filter { it == KeyEventType.KeyDown }
                .onEach { actualOnDown.invoke() }
                .launchIn(this)
        }

        onKeyEvent {
            if (it.key == key) {
                scope.launch { keyEventChannel.send(it.type) }
                true
            } else {
                false
            }
        }
    }
