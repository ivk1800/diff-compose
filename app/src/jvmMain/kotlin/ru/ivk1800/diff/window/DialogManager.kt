package ru.ivk1800.diff.window

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.ivk1800.diff.presentation.DialogRouter

class DialogManager {
    private val _currentDialog = MutableStateFlow<DialogRouter.Dialog?>(null)
    val currentDialog: StateFlow<DialogRouter.Dialog?>
        get() = _currentDialog

    fun show(dialog: DialogRouter.Dialog) {
        _currentDialog.value = dialog
    }

    fun close() {
        _currentDialog.value = null
    }
}
