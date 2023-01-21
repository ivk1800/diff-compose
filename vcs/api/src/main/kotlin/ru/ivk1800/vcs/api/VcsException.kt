package ru.ivk1800.vcs.api

sealed class VcsException(message: String, cause: Throwable?) : RuntimeException(message, cause) {
    class ProcessException(message: String) : VcsException(message, null)
    class ParseException(message: String, cause: Throwable? = null) : VcsException(message, cause)
}
