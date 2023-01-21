package ru.ivk1800.vcs.api.command

/**
 * https://git-scm.com/docs/git-show
 */
interface ShowCommand : Command<List<String>> {
    data class Options(val fileName: String)
}
