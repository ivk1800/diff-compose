package ru.ivk1800.vcs.git.util

import ru.ivk1800.vcs.api.VcsDiff

internal fun VcsDiff.getFileName(): String {
    return when (this) {
        is VcsDiff.Added -> fileName
        is VcsDiff.Deleted -> fileName
        is VcsDiff.Modified -> fileName
        is VcsDiff.Moved -> renameTo
    }
}
