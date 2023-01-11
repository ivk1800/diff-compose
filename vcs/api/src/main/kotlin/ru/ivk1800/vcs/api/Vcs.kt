package ru.ivk1800.vcs.api

import ru.ivk1800.vcs.api.command.DiffCommand
import ru.ivk1800.vcs.api.command.GetUntrackedFilesCommand
import ru.ivk1800.vcs.api.command.HashObjectCommand
import ru.ivk1800.vcs.api.command.ShowCommand
import ru.ivk1800.vcs.api.command.UpdateIndexCommand
import java.io.File
import java.nio.file.Path

interface Vcs {
    suspend fun isRepository(directory: File): Boolean

    suspend fun getCommits(directory: File, branchName: String, limit: Int, offset: Int): List<VcsCommit>

    suspend fun getCommit(directory: File, hash: String): VcsCommit?

    suspend fun getCommitFiles(directory: File, commitHash: String): List<VcsFile>

    suspend fun getDiff(directory: File, oldCommitHash: String, newCommitHash: String, filePath: String): VcsDiff

    suspend fun getDiffCommand(directory: Path, options: DiffCommand.Options): DiffCommand

    suspend fun getDiff(directory: File, oldBlobId: String, newBlobId: String): VcsDiff

    suspend fun getUnStagedDiff(directory: File): List<VcsDiff>

    suspend fun getStagedDiff(directory: File): List<VcsDiff>

    suspend fun removeAllFromStaged(directory: File)

    suspend fun addAllToStaged(directory: File)

    suspend fun removeFilesFromStaged(directory: File, filePaths: List<String>)

    suspend fun addFilesToStaged(directory: File, filePaths: List<String>)

    suspend fun getHashObjectCommand(directory: Path, options: HashObjectCommand.Options): String

    suspend fun getShowCommand(directory: Path, options: ShowCommand.Options): ShowCommand

    suspend fun getUpdateIndexCommand(directory: Path, options: UpdateIndexCommand.Options): UpdateIndexCommand

    suspend fun getUntrackedFilesCommand(directory: Path): GetUntrackedFilesCommand
}
