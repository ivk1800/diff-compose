import ru.ivk1800.vcs.api.VcsException
        check(raw.isNotBlank()) { "Empty diff cannot be parsed" }

                            RawResult.RawHunk.Line.Type.NoNewline -> VcsHunkLine.Type.NoNewline
                fileName = requireNotNull(result.bFileName) { "fileName is null" },
                oldId = requireNotNull(result.oldId) { "oldId is null" },
                newId = requireNotNull(result.newId) { "newId is null" },
        val aStart = requireNotNull(aRaw[0].toIntOrNull()) {
            "Unable to parse range of first file"
        }
        val aEnd = aStart + requireNotNull(aRaw[1].toIntOrNull()) {
            "Unable to parse range of first file"
        } - 1
            aStart,
            aEnd,
        val bStart = requireNotNull(bRaw[0].toIntOrNull()) {
            "Unable to parse range of second file"
        }
        val bEnd = bStart + requireNotNull(bRaw[1].toIntOrNull()) {
            "Unable to parse range of second file"
        } - 1
        val bRange = IntRange(bStart, bEnd)
                    '\\' -> RawResult.RawHunk.Line.Type.NoNewline
                    NoNewline,