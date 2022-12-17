package ru.ivk1800.vcs.api

import java.io.File

interface Vcs {
    suspend fun isRepository(directory: File): Boolean

    suspend fun getCommits(directory: File, branchName: String, limit: Int, offset: Int): List<VcsCommit>
}
