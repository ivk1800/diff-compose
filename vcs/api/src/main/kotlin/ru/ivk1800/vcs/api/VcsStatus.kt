package ru.ivk1800.vcs.api

data class VcsStatus(
    val branch: String,
    val staged: List<VcsFile>,
    val unstaged: List<VcsFile>,
    val untracked: List<VcsFile>,
)
