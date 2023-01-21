package ru.ivk1800.vcs.git.parser

import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.VcsStash

internal class GitStashListParser {
    private val stashRegex = "stash@\\{(\\d+)}: (.*)".toRegex()

    fun parse(raw: String): List<VcsStash> =
        runCatching { parseInternal(raw) }.getOrElse { error ->
            throw VcsException.ParseException(message = "An error occurred while parsing stash list", cause = error)
        }

    private fun parseInternal(raw: String): List<VcsStash> {
        return stashRegex.findAll(raw).map { result ->
            val groups = result.groups
            check(groups.size == 3) { "Unable parse stash" }
            val id = groups[1]?.value?.toIntOrNull() ?: error("Unable parse stash id")
            val message = groups[2]?.value ?: error("Unable parse stash message")
            check(message.isNotEmpty()) { "Message of stash cannot be empty" }

            VcsStash(id, message)
        }.toList()
    }
}
