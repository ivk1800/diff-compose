package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem

class DiffInfoItemMapper {
    fun mapToItems(diff: Diff): ImmutableList<DiffInfoItem> =
        diff.hunks.map { hunk ->
            mutableListOf<DiffInfoItem>(DiffInfoItem.HunkHeader("hunk")).apply {
                hunk.lines.forEach { line ->
                    add(
                        DiffInfoItem.Line(
                            text = line.text,
                            type = when (line.type) {
                                Diff.Hunk.Line.Type.NotChanged -> DiffInfoItem.Line.Type.NotChanged
                                Diff.Hunk.Line.Type.Added -> DiffInfoItem.Line.Type.Added
                                Diff.Hunk.Line.Type.Removed -> DiffInfoItem.Line.Type.Removed
                            }
                        )
                    )
                }
            }
        }
            .flatten()
            .toImmutableList()
}
