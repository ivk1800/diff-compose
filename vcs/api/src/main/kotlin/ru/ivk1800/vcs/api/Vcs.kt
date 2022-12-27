package ru.ivk1800.vcs.api

import java.io.File

interface Vcs {
    suspend fun isRepository(directory: File): Boolean

    suspend fun getCommits(directory: File, branchName: String, limit: Int, offset: Int): List<VcsCommit>

    suspend fun getCommit(directory: File, hash: String): VcsCommit?

    suspend fun getCommitFiles(directory: File, commitHash: String): List<VcsFile>

    suspend fun getDiff(directory: File, oldCommitHash: String, newCommitHash: String, filePath: String): VcsDiff

    suspend fun getUnStagedDiff(directory: File): List<VcsDiff>

    suspend fun getStagedDiff(directory: File): List<VcsDiff>

    suspend fun removeAllFromStaged(directory: File)

    suspend fun addAllToStaged(directory: File)
}
