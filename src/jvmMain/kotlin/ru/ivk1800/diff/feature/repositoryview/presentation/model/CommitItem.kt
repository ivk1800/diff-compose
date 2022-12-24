package ru.ivk1800.diff.feature.repositoryview.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class CommitItem(
    val id: CommitId,
    val description: String,
    val commit: String,
    val author: String,
    val date: String,
)
