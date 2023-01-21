package ru.ivk1800.vcs.api.command

/**
 * https://git-scm.com/docs/git-hash-object
 */
interface HashObjectCommand : Command<String> {
    data class Options(val content: String)
}