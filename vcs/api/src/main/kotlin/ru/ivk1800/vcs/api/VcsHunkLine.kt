package ru.ivk1800.vcs.api

data class VcsHunkLine(val text: String, val type: Type) {
    enum class Type {
        NotChanged,
        Added,
        Removed,
        NoNewline,
    }
}