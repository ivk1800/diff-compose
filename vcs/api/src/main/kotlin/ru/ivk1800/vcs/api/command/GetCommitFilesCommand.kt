package ru.ivk1800.vcs.api.command

import ru.ivk1800.vcs.api.VcsFile

interface GetCommitFilesCommand: Command<List<VcsFile>> {
    data class Options(val hash: String)
}
