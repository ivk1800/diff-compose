package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import kotlin.math.min

class DiffInfoItemMapper {
    fun mapToItems(diff: Diff): ImmutableList<DiffInfoItem> {
        return diff.hunks.mapIndexed { index, hunk ->
            mutableListOf<DiffInfoItem>(mapToHeader(index + 1, hunk)).apply {
                var linesCount = hunk.getStartLineNumber()
                hunk.lines.forEach { line ->
                    val shouldDisplayNumber = line.shouldDisplayNumber()
                    add(
                        DiffInfoItem.Line(
                            number = if (shouldDisplayNumber) {
                                linesCount
                            } else {
                                null
                            },
                            text = line.text,
                            type = when (line.type) {
                                Diff.Hunk.Line.Type.NotChanged -> DiffInfoItem.Line.Type.NotChanged
                                Diff.Hunk.Line.Type.Added -> DiffInfoItem.Line.Type.Added
                                Diff.Hunk.Line.Type.Removed -> DiffInfoItem.Line.Type.Removed
                            }
                        )
                    )
                    if (shouldDisplayNumber) {
                        linesCount++
                    }
                }
            }
        }
            .flatten()
            .toImmutableList()
    }

    private fun mapToHeader(number: Int, hunk: Diff.Hunk): DiffInfoItem.HunkHeader {
        val start = hunk.getStartLineNumber()
        val end = hunk.getEndLineNumber()

        return DiffInfoItem.HunkHeader(
            text ="Hunk ${number}: Lines ${start}:${end}",
            actions = persistentListOf(),
        )
    }

    private fun Diff.Hunk.getStartLineNumber(): Int {
        val firstStartLine = firstRange.first
        val secondStartLine = secondRange.first
        return min(firstStartLine, secondStartLine)
    }

    private fun Diff.Hunk.getEndLineNumber(): Int {
        val firstEndLine = firstRange.first + firstRange.last
        val secondEndLine = secondRange.first + secondRange.last
        return min(firstEndLine, secondEndLine) - 1
    }

    private fun Diff.Hunk.Line.shouldDisplayNumber(): Boolean =
        type == Diff.Hunk.Line.Type.NotChanged || type == Diff.Hunk.Line.Type.Removed
}
