package ru.ivk1800.vcs.git.test

internal class RawDiffBuilder {
    private var diff: String? = null
    private var newFile: String? = null
    private var deletedFile: String? = null
    private var renameFrom: String? = null
    private var renameTo: String? = null
    private var similarityIndex: String? = null
    private var index: String? = null
    private var oldFileName: String? = null
    private var newFileName: String? = null
    private var hunkHeader: String? = null
    private var hunkLines: MutableList<String> = mutableListOf()

    fun diff(old: String, new: String) {
        diff = "diff --git a$old b$new"
    }

    fun newFile(mode: Int) {
        newFile = "new file mode $mode"
    }

    fun deletedFile(mode: Int) {
        deletedFile = "deleted file mode $mode"
    }

    fun renameFrom(name: String) {
        renameFrom = "rename from $name"
    }

    fun renameTo(name: String) {
        renameTo = "rename to $name"
    }

    fun similarityIndex(percent: Int) {
        similarityIndex = "similarity index $percent%"
    }

    fun index(old: String, new: String, mode: Int? = null) {
        index = buildString {
            append("index $old..$new")
            if (mode != null) {
                append(" $mode")
            }
        }
    }

    fun oldFileName(name: String) {
        oldFileName = if (name == "/dev/null") {
            "--- /dev/null"
        } else {
            "--- a$name"
        }
    }

    fun newFileName(name: String) {
        newFileName = if (name == "/dev/null") {
            "+++ /dev/null"
        } else {
            "+++ b$name"
        }
    }

    fun hunkHeader(header: String, line: String? = null) {
        hunkHeader = buildString {
            append(header)
            if (line != null) {
                append(line)
            }
        }
    }

    fun hunkLine(line: String) {
        hunkLines.add(line)
    }

    fun build(): String {
        return buildString {
            diff?.also(this::appendLine)
            renameFrom?.also(this::appendLine)
            renameTo?.also(this::appendLine)
            similarityIndex?.also(this::appendLine)
            newFile?.also(this::appendLine)
            deletedFile?.also(this::appendLine)
            index?.also(this::appendLine)
            oldFileName?.also(this::appendLine)
            newFileName?.also(this::appendLine)
            hunkHeader?.also(this::appendLine)
            hunkLines.forEach(this::appendLine)
        }.trim()
    }
}
