package ru.ivk1800.vcs.api.command

import ru.ivk1800.vcs.api.VcsCommit

interface GetCommitCommand : Command<VcsCommit> {
    data class Options(val hash: String)
}
