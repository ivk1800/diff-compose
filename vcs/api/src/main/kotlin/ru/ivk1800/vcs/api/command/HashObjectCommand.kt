package ru.ivk1800.vcs.api.command

/**
 * https://git-scm.com/docs/git-hash-object
 */
abstract class HashObjectCommand(protected val options: Options) : Command<String> {
    data class Options(val content: String)
}