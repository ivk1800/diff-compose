package ru.ivk1800.diff.presentation

interface DialogRouter {
    fun show(dialog: Dialog)

    sealed interface Dialog {
        data class Error(val title: String, val text: String) : Dialog
        data class Confirmation(
            val title: String,
            val text: String,
            val positiveText: String,
            val negativeText: String,
            val positiveCallback: () -> Unit,
        ) : Dialog
    }
}
