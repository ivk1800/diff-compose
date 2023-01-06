package ru.ivk1800.diff.feature.repositoryview.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class CommitDescription(
    val message: String,
    val commit: String,
    val parents: String,
    val author: String,
    val date: String,
)
