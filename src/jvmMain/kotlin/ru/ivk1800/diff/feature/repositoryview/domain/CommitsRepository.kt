package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import java.io.File
import java.time.Instant

class CommitsRepository(
    private val vcs: Vcs,
) {
    suspend fun getCommits(directory: File, branchName: String, limit: Int, offset: Int): List<Commit> {
        return vcs.getCommits(directory, branchName = branchName, limit = limit, offset = offset).map {
            Commit(
                hash = it.hash,
                abbreviatedHash = it.abbreviatedHash,
                authorName = it.authorName,
                authorEmail = it.authorEmail,
                authorDate = Instant.ofEpochSecond(it.authorDate.toLong()),
                commiterName = it.commiterName,
                commiterEmail = it.commiterEmail,
                commiterDate = Instant.ofEpochSecond(it.commiterDate.toLong()),
                message = it.message,
            )
        }
    }
}
