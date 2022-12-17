package ru.ivk1800.diff.feature.repositoryview.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class CommitFileItem(val name: String, val type: Type) {
    enum class Type {
        Edited,
        Added,
        Moved,
    }
}