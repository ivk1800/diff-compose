package ru.ivk1800.vcs.api

data class VcsFile(
    val name: String,
    val changeType: VcsChangeType,
)
