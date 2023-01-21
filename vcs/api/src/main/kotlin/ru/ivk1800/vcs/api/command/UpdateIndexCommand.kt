package ru.ivk1800.vcs.api.command

/**
 * https://git-scm.com/docs/git-update-index)
 */
interface UpdateIndexCommand : Command<Unit> {
    data class Options(val fileName: String, val content: String)
}
