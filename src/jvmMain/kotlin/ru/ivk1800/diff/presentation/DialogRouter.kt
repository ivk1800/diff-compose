package ru.ivk1800.diff.presentation

import androidx.compose.runtime.Immutable

interface DialogRouter {
    fun show(dialog: Dialog)

    @Immutable
    data class Dialog(val title: String, val text: String)
}
