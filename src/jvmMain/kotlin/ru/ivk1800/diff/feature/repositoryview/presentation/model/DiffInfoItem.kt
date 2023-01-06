package ru.ivk1800.diff.feature.repositoryview.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

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

    data class HunkHeader(
        val text: String,
        val actions: ImmutableList<Action>,
    ) : DiffInfoItem {
        enum class Action {
            StageHunk,
            UnstageHunk,
            DiscardHunk,
            DiscardLines,
            StageLines,
            UnstageLines,
        }
    }
}
