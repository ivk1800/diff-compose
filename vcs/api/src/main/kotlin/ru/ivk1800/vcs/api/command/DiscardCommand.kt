package ru.ivk1800.vcs.api.command

abstract class DiscardCommand(protected val options: Options) : Command<Unit> {
    data class Options(val fileName: String, val content: String)
}
