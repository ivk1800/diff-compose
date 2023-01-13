package ru.ivk1800.vcs.git.parser

import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsHunk
import ru.ivk1800.vcs.api.VcsHunkLine
import ru.ivk1800.vcs.git.VcsException

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

        val result = RawResult()

        lines.forEach { line ->
            handleLine(line, result)
        }

        result.submitHunk()

        val hunks = result.hunks.map { hunk ->
            VcsHunk(
                firstRange = hunk.firstRange,
                secondRange = hunk.secondRange,
                lines = hunk.lines.map { line ->
                    VcsHunkLine(
                        text = line.value,
                        type = when (line.type) {
                            RawResult.RawHunk.Line.Type.NotChanged -> VcsHunkLine.Type.NotChanged
                            RawResult.RawHunk.Line.Type.Added -> VcsHunkLine.Type.Added
                            RawResult.RawHunk.Line.Type.Removed -> VcsHunkLine.Type.Removed
                            RawResult.RawHunk.Line.Type.NoNewline -> VcsHunkLine.Type.NoNewline
                        }
                    )
                }
            )
        }

        if (result.newFileMode != null) {
            return VcsDiff.Added(
                fileName = requireNotNull(result.bFileName),
                oldId = requireNotNull(result.oldId),
                newId = requireNotNull(result.newId),
                hunks = hunks,
            )
        } else if (result.deletedFileMode != null) {
            return VcsDiff.Deleted(
                fileName = requireNotNull(result.bFileName),
                oldId = requireNotNull(result.oldId),
                newId = requireNotNull(result.newId),
                hunks = hunks,
            )
        } else if (result.renameFrom != null && result.renameTo != null) {
            return VcsDiff.Moved(
                renameFrom = requireNotNull(result.renameFrom),
                renameTo = requireNotNull(result.renameTo),
                oldId = requireNotNull(result.oldId),
                newId = requireNotNull(result.newId),
                hunks = hunks,
            )
        } else {
            return VcsDiff.Modified(
                fileName = requireNotNull(result.bFileName),
                oldId = requireNotNull(result.oldId),
                newId = requireNotNull(result.newId),
                hunks = hunks,
            )
        }
    }

    private fun handleLine(line: String, result: RawResult) {
        if (matchFileName(line)) {
            parseFileName(line, result)
        } else if (matchIndex(line)) {
            parseIndex(line, result)
        } else if (matchNewFileMode(line)) {
            parseNewFileMode(line, result)
        } else if (matchRenameFrom(line)) {
            parseRenameFrom(line, result)
        } else if (matchRenameTo(line)) {
            parseRenameTo(line, result)
        } else if (matchSimilarityIndex(line)) {
            parseSimilarityIndex(line, result)
        } else if (matchDeletedFileMode(line)) {
            parseDeletedFileMode(line, result)
        } else if (matchOldFileName(line)) {
            parseOldFileName(line, result)
        } else if (matchNewFileName(line)) {
            parseNewFileName(line, result)
        } else if (matchHeaderHunk(line)) {
            result.submitHunk()
            parseHunkHeader(line, result)
        } else if (matchHunkLine(line)) {
            parseHunkLine(line, result)
        } else if (line.isBlank()) {
            // skip
        } else {
            error("Unable to parse diff, unhandled line: $line")
        }
    }

    private fun parseHunkHeader(line: String, result: RawResult) {
        require(line.isNotEmpty()) { "Unable to parse hunk header, line is empty" }

        // @@ -7,6 +7,7 @@ import ru.ivk1800.vcs.api.VcsCommit
        val lineAfterHeader = line.substringAfter(" @@")

        // TODO: improve parsing
        val r = line.substringAfter("@@ -").substringBefore(" @@").split(" +")
        val aRaw = r[0].split(",")
        val bRaw = r[1].split(",")

        val aRange = IntRange(
            requireNotNull(aRaw[0].toIntOrNull()) {
                "Unable to parse range of first file"
            },
            requireNotNull(aRaw[1].toIntOrNull()) {
                "Unable to parse range of first file"
            },
        )
        val bRange = IntRange(
            requireNotNull(bRaw[0].toIntOrNull()) {
                "Unable to parse range of second file"
            },
            requireNotNull(bRaw[1].toIntOrNull()) {
                "Unable to parse range of second file"
            },
        )
        result.setHunkInfo(aRange, bRange)

//        if (lineAfterHeader.isNotEmpty()) {
//            parseHunkLine(lineAfterHeader, result)
//        }
    }

    private fun parseHunkLine(line: String, result: RawResult) {
        require(line.isNotEmpty()) { "Unable to parse hunk line, line is empty" }

        val lineChar: Char = line[0]

        result.addHunkLine(
            RawResult.RawHunk.Line(
                type = when (lineChar) {
                    '\\' -> RawResult.RawHunk.Line.Type.NoNewline
                    ' ' -> RawResult.RawHunk.Line.Type.NotChanged

                    '+' -> RawResult.RawHunk.Line.Type.Added
                    '-' -> RawResult.RawHunk.Line.Type.Removed
                    else -> error("Unable to parse hunk line, unknown char: $lineChar")
                },
                value = line.removePrefix(lineChar.toString()),
            )
        )
    }

    private fun matchFileName(line: String): Boolean = line.startsWith("diff --git ")

    private fun matchOldFileName(line: String): Boolean = line.startsWith("--- ")

    private fun matchNewFileName(line: String): Boolean = line.startsWith("+++ ")

    private fun matchIndex(line: String): Boolean = line.startsWith("index ")

    private fun matchNewFileMode(line: String): Boolean = line.startsWith("new file mode ")

    private fun matchRenameFrom(line: String): Boolean = line.startsWith("rename from ")

    private fun matchRenameTo(line: String): Boolean = line.startsWith("rename to ")

    private fun matchSimilarityIndex(line: String): Boolean = line.startsWith("similarity index ")

    private fun matchDeletedFileMode(line: String): Boolean = line.startsWith("deleted file mode ")

    private fun matchHeaderHunk(line: String): Boolean = line.startsWith("@@ ")

    private fun matchHunkLine(line: String): Boolean =
        line.startsWith(" ") ||
                line.startsWith("-") ||
                line.startsWith("+") ||
                line.startsWith("\\")

    private fun parseFileName(line: String, outResult: RawResult) {
        outResult.aFileName = line.substringAfter("diff --git a/").substringBefore(" b/")
        outResult.bFileName = line.substringAfter(" b/")
    }

    private fun parseRenameFrom(line: String, outResult: RawResult) {
        outResult.renameFrom = line.substringAfter("rename from ")
    }

    private fun parseRenameTo(line: String, outResult: RawResult) {
        outResult.renameTo = line.substringAfter("rename to ")
    }

    private fun parseSimilarityIndex(line: String, outResult: RawResult) {

    }

    private fun parseOldFileName(line: String, outResult: RawResult) {
        var oldName = line.substringAfter("--- ")

        if (oldName.startsWith("a")) {
            oldName = oldName.substringAfter("a/")
        }

        outResult.oldFileName = oldName
    }

    private fun parseNewFileName(line: String, outResult: RawResult) {
        var newName = line.substringAfter("+++ ")

        if (newName.startsWith("b")) {
            newName = newName.substringAfter("b/")
        }

        outResult.newFileName = newName
    }

    private fun parseIndex(line: String, outResult: RawResult) {
        val oldId = line.substringAfter("index ").substringBefore("..")
        val newId = line.substringAfter("..").substringBefore(" ")

        outResult.oldId = oldId
        outResult.newId = newId
    }

    private fun parseNewFileMode(line: String, outResult: RawResult) {
        val newFileMode = line.substringAfter("new file mode ")

        outResult.newFileMode = newFileMode.toIntOrNull()
    }

    private fun parseDeletedFileMode(line: String, outResult: RawResult) {
        val deletedFileMode = line.substringAfter("deleted file mode ")

        outResult.deletedFileMode = deletedFileMode.toIntOrNull()
    }

    private class RawResult(
        var aFileName: String? = null,
        var bFileName: String? = null,
        var oldFileName: String? = null,
        var newFileName: String? = null,
        var oldId: String? = null,
        var newId: String? = null,
        var newFileMode: Int? = null,
        var deletedFileMode: Int? = null,
        var renameFrom: String? = null,
        var renameTo: String? = null,
        val hunks: MutableList<RawHunk> = mutableListOf(),
    ) {
        private var currentHunk = RawHunk()

        fun addHunkLine(line: RawHunk.Line) {
            currentHunk.lines.add(line)
        }

        fun setHunkInfo(firstRange: IntRange, secondRange: IntRange) {
            currentHunk.firstRange = firstRange
            currentHunk.secondRange = secondRange
        }

        fun submitHunk() {
            if (currentHunk.lines.isNotEmpty()) {
                hunks.add(currentHunk)
                currentHunk = RawHunk()
            }
        }

        class RawHunk(
            var firstRange: IntRange = IntRange.EMPTY,
            var secondRange: IntRange = IntRange.EMPTY,
            val lines: MutableList<Line> = mutableListOf(),
        ) {
            class Line(
                val type: Type,
                val value: String,
            ) {
                enum class Type {
                    NotChanged,
                    Added,
                    Removed,
                    NoNewline,
                }
            }
        }
    }
}
