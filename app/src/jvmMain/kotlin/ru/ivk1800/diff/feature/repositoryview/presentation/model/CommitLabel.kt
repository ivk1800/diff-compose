package ru.ivk1800.diff.feature.repositoryview.presentation.model

sealed interface CommitLabel {
    data class Branch(val name: String): CommitLabel
}
