package ru.ivk1800.diff.feature.repositoryview.domain

data class Status(
    val staged: List<CommitFile>,
    val unstaged: List<CommitFile>,
    val untracked: List<CommitFile>,
)
