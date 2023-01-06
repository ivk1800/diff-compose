package ru.ivk1800.diff.feature.repositoryview.domain

data class Diff(
    val filePath: String,
    val oldId: String,
    val newId: String,
    val hunks: List<Hunk>,
) {
    data class Hunk(
        val firstRange: IntRange,
        val secondRange: IntRange,
        val lines: List<Line>,
    ) {
        data class Line(val text: String, val type: Type) {
            enum class Type {
                NotChanged,
                Added,
                Removed,
            }
        }
    }
}
