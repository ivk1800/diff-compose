package ru.ivk1800.diff.feature.repositoryview.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface DiffInfoItem {

    val id: Id

    data class Line(
        override val id: Id.Line,
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
        override val id: Id.Header,
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

    sealed interface Id {
        data class Header(
            val number: Int,
        ) : Id

        data class Line(val number: Int) : Id
    }
}
