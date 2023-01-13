package ru.ivk1800.vcs.api.command

/**
 * https://git-scm.com/docs/git-update-index)
 */
abstract class UpdateIndexCommand(protected val options: Options) : Command<Unit> {
    data class Options(val fileName: String, val content: String)
}
