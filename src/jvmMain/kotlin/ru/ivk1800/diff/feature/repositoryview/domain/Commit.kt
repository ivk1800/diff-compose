package ru.ivk1800.diff.feature.repositoryview.domain

import java.time.Instant

data class Commit(
    val hash: String,
    val parents: List<String>,
    val abbreviatedHash: String,
    val authorName: String,
    val authorEmail: String,
    val authorDate: Instant,
    val commiterName: String,
    val commiterEmail: String,
    val commiterDate: Instant,
    val message: String,
)
