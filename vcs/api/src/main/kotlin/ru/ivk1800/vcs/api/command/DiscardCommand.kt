package ru.ivk1800.vcs.api.command

interface DiscardCommand : Command<Unit> {
    data class Options(val fileName: String, val content: String)
}
