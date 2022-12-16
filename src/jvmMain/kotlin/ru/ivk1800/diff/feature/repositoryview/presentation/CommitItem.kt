package ru.ivk1800.diff.feature.repositoryview.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class CommitItem(val description: String, val commit: String, val author: String, val date: String)