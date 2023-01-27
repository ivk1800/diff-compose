package ru.ivk1800.diff.logging

interface Logger {
    fun e(error: Throwable, tag: String, message: String?)

    fun d(tag: String, message: String)
}
