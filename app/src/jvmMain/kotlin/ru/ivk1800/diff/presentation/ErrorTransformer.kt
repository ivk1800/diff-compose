package ru.ivk1800.diff.presentation

class ErrorTransformer {
    fun transformForDisplay(error: Throwable): String = getMessage(error)

    private fun getMessage(e: Throwable): String {
        val cause = e.cause
        return if (cause == null) {
            e.message.orEmpty()
        } else {
            listOf(e.message.orEmpty(), getMessage(cause)).joinToString("\n")
        }
    }
}
