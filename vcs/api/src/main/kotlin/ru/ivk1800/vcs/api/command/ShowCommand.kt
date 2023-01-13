package ru.ivk1800.vcs.api.command

/**
 * https://git-scm.com/docs/git-show
 */
abstract class ShowCommand(protected val options: Options) : Command<List<String>> {
    data class Options(val fileName: String)
}
