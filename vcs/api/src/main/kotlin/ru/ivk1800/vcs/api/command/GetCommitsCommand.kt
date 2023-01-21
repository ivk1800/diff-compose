package ru.ivk1800.vcs.api.command

import ru.ivk1800.vcs.api.VcsCommit

interface GetCommitsCommand : Command<List<VcsCommit>> {
    data class Options(
        val branchName: String,
        val limit: Int,
        val afterCommit: String?,
    )
}
