package ru.ivk1800.vcs.api.command

interface Command<T> {
    suspend fun run(): T
}
