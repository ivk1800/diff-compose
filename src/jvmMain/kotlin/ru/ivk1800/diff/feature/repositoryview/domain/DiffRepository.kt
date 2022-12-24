package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsHunkLine
import java.io.File

class DiffRepository(
    private val vcs: Vcs,
) {
    suspend fun getDiff(directory: File, oldCommitHash: String, newCommitHash: String, filePath: String): Diff {
        val diff = vcs.getDiff(directory, oldCommitHash, newCommitHash, filePath)
        return Diff(
            hunks = diff.hunks.map { hunk ->
                Diff.Hunk(
                    lines = hunk.lines.map { line ->
                        Diff.Hunk.Line(
                            text = line.text,
                            type = when (line.type) {
                                VcsHunkLine.Type.NotChanged -> Diff.Hunk.Line.Type.NotChanged
                                VcsHunkLine.Type.Added -> Diff.Hunk.Line.Type.Added
                                VcsHunkLine.Type.Removed -> Diff.Hunk.Line.Type.Removed
                            }
                        )
                    }
                )
            }
        )
    }
}
