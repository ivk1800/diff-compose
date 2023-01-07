package ru.ivk1800.diff.feature.repositoryview.presentation.model.ext

import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import kotlin.math.min

fun Diff.Hunk.getStartLineNumber(): Int {
    val firstStartLine = firstRange.first
    val secondStartLine = secondRange.first
    return min(firstStartLine, secondStartLine)
}

fun Diff.Hunk.getEndLineNumber(): Int {
    val firstEndLine = firstRange.first + firstRange.last
    val secondEndLine = secondRange.first + secondRange.last
    return min(firstEndLine, secondEndLine) - 1
}
