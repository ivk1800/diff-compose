package ru.ivk1800.vcs.api

data class VcsCommit(
    val hash: String,
    val abbreviatedHash: String,
    val authorName: String,
    val authorEmail: String,
    val authorDate: Int,
    val commiterName: String,
    val commiterEmail: String,
    val commiterDate: Int,
    val message: String,
)