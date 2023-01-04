package ru.ivk1800.vcs.api

sealed interface VcsDiff {

    val hunks: List<VcsHunk>

    data class Added(
        val fileName: String,
        val oldId: String,
        val newId: String,
        override val hunks: List<VcsHunk>,
    ) : VcsDiff

    data class Modified(
        val fileName: String,
        val oldId: String,
        val newId: String,
        override val hunks: List<VcsHunk>,
    ) : VcsDiff

    data class Moved(
        val renameFrom: String,
        val renameTo: String,
        val oldId: String,
        val newId: String,
        override val hunks: List<VcsHunk>,
    ) : VcsDiff

    data class Deleted(
        val fileName: String,
        val oldId: String,
        val newId: String,
        override val hunks: List<VcsHunk>,
    ) : VcsDiff
}
