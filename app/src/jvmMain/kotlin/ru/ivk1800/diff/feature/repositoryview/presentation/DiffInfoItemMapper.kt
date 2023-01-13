package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.ext.getEndLineNumber
import ru.ivk1800.diff.feature.repositoryview.presentation.model.ext.getStartLineNumber

class DiffInfoItemMapper {
    fun mapToItems(
        type: DiffType,
        diff: Diff,
        selected: ImmutableSet<DiffInfoItem.Id.Line>,
    ): ImmutableList<DiffInfoItem> {
        var totalLineCount = 0
        return diff.hunks.mapIndexed { index, hunk ->
            val hunkHeader = mapToHeader(
                type = type,
                number = index + 1,
                hunk = hunk,
                selected = selected,
            )
            mutableListOf<DiffInfoItem>(hunkHeader).apply {
                var linesCount = hunk.getStartLineNumber()
                hunk.lines.forEach { line ->
                    val shouldDisplayNumber = line.shouldDisplayNumber()
                    add(
                        DiffInfoItem.Line(
                            id = DiffInfoItem.Id.Line(
                                hunkId = hunkHeader.id,
                                number = ++totalLineCount,
                            ),
                            number = if (shouldDisplayNumber) {
                                linesCount
                            } else {
                                null
                            },
                            text = line.text,
                            type = when (line.type) {
                                Diff.Hunk.Line.Type.NotChanged -> DiffInfoItem.Line.Type.NotChanged
                                Diff.Hunk.Line.Type.NoNewline,
                                Diff.Hunk.Line.Type.Added -> DiffInfoItem.Line.Type.NotChanged

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

    private fun mapToHeader(
        type: DiffType,
        number: Int,
        hunk: Diff.Hunk,
        selected: ImmutableSet<DiffInfoItem.Id.Line>,
    ): DiffInfoItem.HunkHeader {
        val start = hunk.getStartLineNumber()
        val end = hunk.getEndLineNumber()

        val id = DiffInfoItem.Id.Hunk(number = number)
        return DiffInfoItem.HunkHeader(
            id = id,
            text = "Hunk ${number}: Lines ${start}:${end}",
            actions = mapHunkActions(type, id, selected),
        )
    }

    private fun mapHunkActions(
        type: DiffType,
        id: DiffInfoItem.Id.Hunk,
        selected: ImmutableSet<DiffInfoItem.Id.Line>,
    ): ImmutableList<DiffInfoItem.HunkHeader.Action> =
        if (selected.any { line -> line.hunkId == id }) {
            when (type) {
                DiffType.Commit -> persistentListOf(
                    DiffInfoItem.HunkHeader.Action.ReverseLines,
                )
                DiffType.UncommittedChanges.Staged -> persistentListOf(
                        DiffInfoItem.HunkHeader.Action.UnstageLines,
                    )
                DiffType.UncommittedChanges.Unstaged -> persistentListOf(
                    DiffInfoItem.HunkHeader.Action.StageLines,
                    DiffInfoItem.HunkHeader.Action.DiscardLines,
                )
            }
        } else {
            when (type) {
                DiffType.Commit -> persistentListOf(DiffInfoItem.HunkHeader.Action.ReverseHunk)
                is DiffType.UncommittedChanges -> when (type) {
                    DiffType.UncommittedChanges.Staged -> persistentListOf(DiffInfoItem.HunkHeader.Action.UnstageHunk)
                    DiffType.UncommittedChanges.Unstaged -> persistentListOf(
                        DiffInfoItem.HunkHeader.Action.StageHunk,
                        DiffInfoItem.HunkHeader.Action.DiscardHunk,
                    )
                }
            }
        }

    private fun Diff.Hunk.Line.shouldDisplayNumber(): Boolean =
        type == Diff.Hunk.Line.Type.NotChanged || type == Diff.Hunk.Line.Type.Removed

    sealed interface DiffType {
        object Commit : DiffType
        sealed interface UncommittedChanges : DiffType {
            object Staged : UncommittedChanges
            object Unstaged : UncommittedChanges
        }
    }
}
