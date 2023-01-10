package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsHunkLine
import ru.ivk1800.vcs.api.command.DiffCommand
import java.io.File

class DiffRepository(
    private val vcs: Vcs,
) {
    suspend fun getDiff(directory: File, oldCommitHash: String, newCommitHash: String, filePath: String): Diff {
        val diff = vcs.getDiff(directory, oldCommitHash, newCommitHash, filePath)
        return toDiff(diff)
    }

    suspend fun getStagedFileDiff(directory: File, fileName: String): Diff {
        val diff = vcs.getDiffCommand(directory.toPath(), DiffCommand.Options.StagedFile(fileName)).run()
        return toDiff(diff)
    }

    suspend fun getUnstagedFileDiff(directory: File, fileName: String): Diff {
        val diff = vcs.getDiffCommand(directory.toPath(), DiffCommand.Options.UnstagedFile(fileName)).run()
        return toDiff(diff)
    }

    suspend fun getDiff(directory: File, oldBlobId: String, newBlobId: String): Diff {
        val diff = vcs.getDiff(directory, oldBlobId, newBlobId)
        return toDiff(diff)
    }

    suspend fun getUnstagedDiff(directory: File): List<Diff> =
        vcs.getUnStagedDiff(directory).map(::toDiff)

    suspend fun getStagedDiff(directory: File): List<Diff> = vcs.getStagedDiff(directory).map(::toDiff)

    suspend fun removeAllFromStaged(directory: File) = vcs.removeAllFromStaged(directory)

    suspend fun addAllToStaged(directory: File) = vcs.addAllToStaged(directory)

    suspend fun removeFilesFromStaged(directory: File, filePaths: List<String>) {
        check(filePaths.isNotEmpty())
        vcs.removeFilesFromStaged(directory, filePaths)
    }

    suspend fun addFilesToStaged(directory: File, filePaths: List<String>) {
        check(filePaths.isNotEmpty())
        vcs.addFilesToStaged(directory, filePaths)
    }

    private fun toDiff(diff: VcsDiff): Diff {
        return Diff(
            filePath = when (diff) {
                is VcsDiff.Added -> diff.fileName
                is VcsDiff.Deleted -> diff.fileName
                is VcsDiff.Modified -> diff.fileName
                is VcsDiff.Moved -> diff.renameTo
            },
            oldId = when (diff) {
                is VcsDiff.Added -> diff.oldId
                is VcsDiff.Deleted -> diff.oldId
                is VcsDiff.Modified -> diff.oldId
                is VcsDiff.Moved -> diff.oldId
            },
            newId = when (diff) {
                is VcsDiff.Added -> diff.newId
                is VcsDiff.Deleted -> diff.newId
                is VcsDiff.Modified -> diff.newId
                is VcsDiff.Moved -> diff.newId
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

