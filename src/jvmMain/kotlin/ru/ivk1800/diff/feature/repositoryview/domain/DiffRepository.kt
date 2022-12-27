package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsHunkLine
import java.io.File

class DiffRepository(
    private val vcs: Vcs,
) {
    suspend fun getDiff(directory: File, oldCommitHash: String, newCommitHash: String, filePath: String): Diff {
        val diff = vcs.getDiff(directory, oldCommitHash, newCommitHash, filePath)
        return toDiff(diff)
    }

    suspend fun getUnstagedDiff(directory: File): List<Diff> =
        vcs.getUnStagedDiff(directory).map(::toDiff)

    suspend fun getStagedDiff(directory: File): List<Diff> = vcs.getStagedDiff(directory).map(::toDiff)

    suspend fun removeAllFromStaged(directory: File) = vcs.removeAllFromStaged(directory)

    suspend fun addAllToStaged(directory: File) = vcs.addAllToStaged(directory)

    suspend fun removeFileFromStaged(directory: File, filePath: String) = vcs.removeFileFromStaged(directory, filePath)

    suspend fun addFileToStaged(directory: File, filePath: String) = vcs.addFileToStaged(directory, filePath)

    private fun toDiff(diff: VcsDiff): Diff {
        return Diff(
            filePath = diff.filePath,
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

