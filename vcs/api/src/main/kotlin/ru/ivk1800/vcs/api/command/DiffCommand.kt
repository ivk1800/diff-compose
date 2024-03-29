package ru.ivk1800.vcs.api.command

import ru.ivk1800.vcs.api.VcsDiff

/**
 * https://git-scm.com/docs/git-diff
 */
interface DiffCommand : Command<VcsDiff> {
    sealed interface Options {
        data class StagedFile(val fileName: String): Options
        data class UnstagedFile(val fileName: String): Options
        data class FileInCommit(val oldId: String, val newId: String, val fileName: String): Options
    }
}
