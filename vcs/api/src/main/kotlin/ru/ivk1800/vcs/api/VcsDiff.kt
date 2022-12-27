package ru.ivk1800.vcs.api

data class VcsDiff(
    val filePath: String,
    val hunks: List<VcsHunk>,
)