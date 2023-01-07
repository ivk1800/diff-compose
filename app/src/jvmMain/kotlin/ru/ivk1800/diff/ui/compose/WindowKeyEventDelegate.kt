package ru.ivk1800.diff.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class WindowKeyEventDelegate(
    private val test: (KeyEvent) -> Boolean,
    private val onDown: () -> Unit,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val keyEventChannel = Channel<KeyEventType>()

    init {
        keyEventChannel.consumeAsFlow()
            .filter { it == KeyEventType.KeyUp || it == KeyEventType.KeyDown }
            .distinctUntilChanged()
            .filter { it == KeyEventType.KeyDown }
            .onEach { onDown.invoke() }
            .launchIn(scope)
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        return if (test.invoke(event)) {
            scope.launch { keyEventChannel.send(event.type) }
            true
        } else {
            false
        }
    }

    fun dispose() {
        scope.cancel()
        keyEventChannel.close()
    }
}

@Composable
fun rememberWindowKeyEventDelegate(
    test: (KeyEvent) -> Boolean,
    onDown: () -> Unit,
): WindowKeyEventDelegate {
    val delegate = remember { WindowKeyEventDelegate(test, onDown) }

    DisposableEffect(key1 = delegate) {
        onDispose { delegate.dispose() }
    }

    return delegate
}