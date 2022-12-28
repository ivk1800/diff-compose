package ru.ivk1800.vcs.git

import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsHunk
import ru.ivk1800.vcs.api.VcsHunkLine

internal class VcsDiffParser {

    @Throws(VcsException.ParseException::class)
    fun parseSingle(raw: String): VcsDiff {
        return runCatching { parseInternal(raw) }.getOrElse { error ->
            throw VcsException.ParseException(message = "An error occurred while parsing the diff", cause = error)
        }
    }

    @Throws(VcsException.ParseException::class)
    fun parseMultiple(raw: String): List<VcsDiff> {
        if (raw.isEmpty()) {
            return emptyList()
        }
        return runCatching { parseMultipleInternal(raw) }.getOrElse { error ->
            throw VcsException.ParseException(message = "An error occurred while parsing the diff", cause = error)
        }
    }

    @Throws(VcsException.ParseException::class)
    private fun parseMultipleInternal(raw: String): List<VcsDiff> {
        val diffs = mutableListOf<String>()

        val currentDiff = mutableListOf<String>()

        raw.lines().forEach { line ->
            if (line.startsWith("diff --git ") && currentDiff.isNotEmpty()) {
                diffs.add(currentDiff.joinToString("\n"))
                currentDiff.clear()
            }
            currentDiff.add(line)
        }
        diffs.add(currentDiff.joinToString("\n"))
        currentDiff.clear()

        check(diffs.joinToString("\n") == raw)
        return diffs.map(::parseInternal)
    }

    private fun parseInternal(raw: String): VcsDiff {
        val lines: List<String> = raw.lines()

        val hunks = mutableListOf<VcsHunk>()

        val currentHunkLines = mutableListOf<VcsHunkLine>()
        var isHunk = false

        fun addHunk() {
            hunks.add(
                VcsHunk(
                    currentHunkLines.toList()
                )
            )
            currentHunkLines.clear()
        }

        var filePath: String? = null

        lines.forEach { line ->
            if (line.startsWith("@@")) {
                if (!isHunk) {
                    isHunk = true
                } else {
                    addHunk()
                }
            } else if (line.startsWith("--- a/")) {
                filePath = line.substringAfter("--- a/")
            } else {
                if (isHunk) {
                    if (line.isEmpty()) {
                        currentHunkLines.add(
                            VcsHunkLine(
                                line,
                                type = VcsHunkLine.Type.NotChanged
                            )
                        )
                    } else {
                        val lineChar: Char = line[0]

                        currentHunkLines.add(
                            VcsHunkLine(
                                line.removePrefix(lineChar.toString()),
                                type = when (lineChar) {
                                    '\\',
                                    ' ' -> VcsHunkLine.Type.NotChanged

                                    '+' -> VcsHunkLine.Type.Added
                                    '-' -> VcsHunkLine.Type.Removed
                                    else -> error("Unknown char: $lineChar")
                                }
                            )
                        )
                    }
                }
            }
        }

        if (currentHunkLines.isNotEmpty()) {
            addHunk()
        }

        return VcsDiff(
            filePath = requireNotNull(filePath) {
                "Unable parse filePath"
            },
            hunks,
        )
    }
}
