package ru.ivk1800.vcs.api

data class VcsHunk(
    val firstRange: IntRange,
    val secondRange: IntRange,
    val lines: List<VcsHunkLine>,
)
