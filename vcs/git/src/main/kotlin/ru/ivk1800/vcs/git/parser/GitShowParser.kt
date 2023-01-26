package ru.ivk1800.vcs.git.parser

import ru.ivk1800.vcs.api.VcsChangeType
import ru.ivk1800.vcs.api.VcsFile

internal class GitShowParser {
    fun parseFiles(raw: String): List<VcsFile> =
        raw
            .lines()
            .drop(1)
            .filter(CharSequence::isNotEmpty)
            .map {line ->
                val parts = line.split("\t")
                // TODO: support complex format https://git-scm.com/docs/git-status#_changed_tracked_entries
                require(parts.size == 2) {
                    "Unable parse line of file: $line"
                }
                VcsFile(
                    name = parts[1],
                    changeType = parts[0].toChangeType(),
                )
            }

    private fun String.toChangeType(): VcsChangeType =
        when(this) {
            "A" -> VcsChangeType.Add
            "C" -> VcsChangeType.Copy
            "D" -> VcsChangeType.Delete
            "M" -> VcsChangeType.Modify
            "R" -> VcsChangeType.Rename
            else -> error("Unknown change type: $this")
        }
}
