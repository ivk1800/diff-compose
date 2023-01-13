package ru.ivk1800.diff.feature.repositoryview.presentation.model.ext

import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import kotlin.math.max
import kotlin.math.min

fun Diff.Hunk.getStartLineNumber(): Int {
    val firstStartLine = firstRange.first
    val secondStartLine = secondRange.first
    return min(firstStartLine, secondStartLine)
}

fun Diff.Hunk.getEndLineNumber(): Int {
    val firstEndLine = firstRange.last
    val secondEndLine = secondRange.last
    return max(firstEndLine, secondEndLine)
}
