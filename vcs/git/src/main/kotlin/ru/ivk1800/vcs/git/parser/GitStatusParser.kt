package ru.ivk1800.vcs.git.parser

import ru.ivk1800.vcs.api.VcsChangeType
import ru.ivk1800.vcs.api.VcsFile
import ru.ivk1800.vcs.api.VcsStatus
import ru.ivk1800.vcs.git.VcsException

internal class GitStatusParser {
    @Throws(VcsException.ParseException::class)
    fun parse(raw: String): VcsStatus =
        runCatching { parseInternal(raw) }.getOrElse { error ->
            throw VcsException.ParseException(message = "An error occurred while parsing status", cause = error)
        }

    private fun parseInternal(raw: String): VcsStatus {
        val lines = raw.split(System.lineSeparator())

        val iterator: Iterator<String> = lines.iterator()

        var stagedFiles = emptyList<VcsFile>()
        var unstagedFiles = emptyList<VcsFile>()
        var untrackedFiles = emptyList<VcsFile>()

        while (iterator.hasNext()) {
            when (iterator.next()) {
                StagedFilesRegion -> {
                    stagedFiles = parseStagedFiles(iterator)
                }
                UnstagedFilesRegion -> {
                    unstagedFiles = parseUnstagedFiles(iterator)
                }
                UntrackedFilesRegion -> {
                    untrackedFiles = parseUntrackedFiles(iterator)
                }
            }
        }

        return VcsStatus(
            staged = stagedFiles,
            unstaged = unstagedFiles,
            untracked = untrackedFiles,
        )
    }

    private fun parseStagedFiles(lines: Iterator<String>): List<VcsFile> =
        runCatching { parseFiles(lines) }.getOrElse { cause ->
            throw IllegalStateException("An error occurred while parsing unstaged files", cause)
        }

    private fun parseUnstagedFiles(lines: Iterator<String>): List<VcsFile> =
        runCatching { parseFiles(lines) }.getOrElse { cause ->
            throw IllegalStateException("An error occurred while parsing untracked files", cause)
        }

    private fun parseUntrackedFiles(lines: Iterator<String>): List<VcsFile> {
        val files = mutableListOf<VcsFile>()

        while (lines.hasNext()) {
            val line = lines.next()
            if (line.isEmpty()) {
                return files
            } else {
                files.add(
                    VcsFile(
                        name = line.trim(),
                        changeType = VcsChangeType.Add,
                    )
                )
            }
        }

        return files
    }

    private fun parseFiles(lines: Iterator<String>): List<VcsFile> {
        val files = mutableListOf<VcsFile>()

        while (lines.hasNext()) {
            val line = lines.next()
            if (line.isEmpty()) {
                return files
            } else {
                val parts = line.trim().split(":   ")
                check(parts.size == 2) {
                    "Unable parse line of file: $line"
                }
                val changeType = when (val rawChangeType = parts[0]) {
                    "renamed" -> VcsChangeType.Rename
                    "deleted" -> VcsChangeType.Delete
                    "modified" -> VcsChangeType.Modify
                    "copied" -> VcsChangeType.Copy
                    "new file" -> VcsChangeType.Add
                    else -> error("Unknown change type: $rawChangeType")
                }
                val name = when (changeType) {
                    VcsChangeType.Copy,
                    VcsChangeType.Add,
                    VcsChangeType.Modify,
                    VcsChangeType.Delete -> parts[1]
                    VcsChangeType.Rename -> {
                        val renamedParts = parts[1].split(" -> ")
                        check(renamedParts.size == 2) {
                            "Unable parse line of renamed file: $line"
                        }
                        renamedParts[1]
                    }
                }

                files.add(
                    VcsFile(
                        name = name,
                        changeType = changeType,
                    )
                )
            }
        }

        return files
    }

    private companion object {
        private const val StagedFilesRegion = "  (use \"git restore --staged <file>...\" to unstage)"
        private const val UnstagedFilesRegion = "  (use \"git restore <file>...\" to discard changes in working directory)"
        private const val UntrackedFilesRegion = "  (use \"git add <file>...\" to include in what will be committed)"
    }
}
