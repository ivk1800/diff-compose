package ru.ivk1800.diff.feature.repositoryview.domain

data class Status(
    var branch: String,
    val staged: List<CommitFile>,
    val unstaged: List<CommitFile>,
    val untracked: List<CommitFile>,
)
