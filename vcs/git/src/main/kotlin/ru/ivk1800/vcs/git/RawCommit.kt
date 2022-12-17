package ru.ivk1800.vcs.git

internal data class RawCommit(
    val hash: String,
    val abbreviatedHash: String,
    val authorName: String,
    val authorEmail: String,
    val authorDate: String,
    val commiterName: String,
    val commiterEmail: String,
    val commiterDate: String,
    val message: String,
)