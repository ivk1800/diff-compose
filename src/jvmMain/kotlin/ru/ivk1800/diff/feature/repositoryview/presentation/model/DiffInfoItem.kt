package ru.ivk1800.diff.feature.repositoryview.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface DiffInfoItem {
    data class Line(
        val number: Int?,
        val text: String,
        val type: Type,
    ) : DiffInfoItem {
        enum class Type {
            NotChanged,
            Added,
            Removed,
        }
    }

    data class HunkHeader(val text: String) : DiffInfoItem
}