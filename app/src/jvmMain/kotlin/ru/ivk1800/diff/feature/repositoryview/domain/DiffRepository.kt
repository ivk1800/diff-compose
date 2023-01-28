package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsHunkLine
import ru.ivk1800.vcs.api.command.DiffCommand
import java.io.File

class DiffRepository(
    private val repoDirectory: File,
    private val vcs: Vcs,
) {
    suspend fun getDiff(oldCommitHash: String, newCommitHash: String, filePath: String): Diff {
        val diff = vcs.getDiffCommand(
            repoDirectory.toPath(),
            DiffCommand.Options.FileInCommit(oldCommitHash, newCommitHash, filePath),
        ).run()
        return toDiff(diff)
    }

    suspend fun getStagedFileDiff(fileName: String): Diff {
        val diff = vcs.getDiffCommand(repoDirectory.toPath(), DiffCommand.Options.StagedFile(fileName)).run()
        return toDiff(diff)
    }

    suspend fun getUnstagedFileDiff(fileName: String): Diff {
        val diff = vcs.getDiffCommand(repoDirectory.toPath(), DiffCommand.Options.UnstagedFile(fileName)).run()
        return toDiff(diff)
    }

    suspend fun getDiff(oldBlobId: String, newBlobId: String): Diff {
        val diff = vcs.getDiff(repoDirectory, oldBlobId, newBlobId)
        return toDiff(diff)
    }

    suspend fun getUnstagedDiff(): List<Diff> =
        vcs.getUnStagedDiff(repoDirectory).map(::toDiff)

    suspend fun getStagedDiff(): List<Diff> = vcs.getStagedDiff(repoDirectory).map(::toDiff)

    private fun toDiff(diff: VcsDiff): Diff {
        return Diff(
            filePath = when (diff) {
                is VcsDiff.Added -> diff.fileName
                is VcsDiff.Deleted -> diff.fileName
                is VcsDiff.Modified -> diff.fileName
                is VcsDiff.Moved -> diff.renameTo
            },
            hunks = diff.hunks.map { hunk ->
                Diff.Hunk(
                    firstRange = hunk.firstRange,
                    secondRange = hunk.secondRange,
                    lines = hunk.lines.map { line ->
                        Diff.Hunk.Line(
                            text = line.text,
                            type = when (line.type) {
                                VcsHunkLine.Type.NotChanged -> Diff.Hunk.Line.Type.NotChanged
                                VcsHunkLine.Type.Added -> Diff.Hunk.Line.Type.Added
                                VcsHunkLine.Type.Removed -> Diff.Hunk.Line.Type.Removed
                                VcsHunkLine.Type.NoNewline -> Diff.Hunk.Line.Type.NoNewline
                            }
                        )
                    }
                )
            },
            changeType = when (diff) {
                is VcsDiff.Added -> ChangeType.Add
                is VcsDiff.Deleted -> ChangeType.Delete
                is VcsDiff.Modified -> ChangeType.Modify
                is VcsDiff.Moved -> ChangeType.Rename
            },
        )
    }
}

