package ru.ivk1800.vcs.git

import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsHunk
import ru.ivk1800.vcs.api.VcsHunkLine

internal class VcsDiffParser {

    fun parse(raw: String): VcsDiff {
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

        lines.forEach { line ->
            if (line.startsWith("@@")) {
                if (!isHunk) {
                    isHunk = true
                } else {
                    addHunk()
                }
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

        return VcsDiff(hunks)
    }
}