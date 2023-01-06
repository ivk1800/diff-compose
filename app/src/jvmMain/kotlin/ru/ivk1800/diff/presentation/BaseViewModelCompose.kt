package ru.ivk1800.diff.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

@Composable
fun <VM : BaseViewModel> viewModel(factory: () -> VM): VM {
    val vm = remember { factory.invoke() }

    DisposableEffect(key1 = vm) {
        onDispose { vm.dispose() }
    }

    return vm
}