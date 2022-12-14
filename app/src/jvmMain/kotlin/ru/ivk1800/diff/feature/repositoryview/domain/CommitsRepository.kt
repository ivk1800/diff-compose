package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsChangeType
import ru.ivk1800.vcs.api.VcsCommit
import java.io.File
import java.time.Instant

class CommitsRepository(
    private val vcs: Vcs,
) {
    suspend fun getCommit(directory: File, hash: String): Commit? =
        vcs.getCommit(directory, hash)?.toCommit()

    suspend fun getCommits(directory: File, branchName: String, limit: Int, offset: Int): List<Commit> =
        vcs.getCommits(directory, branchName = branchName, limit = limit, offset = offset).map { it.toCommit() }

    suspend fun getCommitFiles(directory: File, commitHash: String): List<CommitFile> =
        vcs.getCommitFiles(directory, commitHash).map { file ->
            CommitFile(
                name = file.name,
                changeType = file.changeType.toChangeType(),
            )
        }

    private fun VcsCommit.toCommit(): Commit =
        Commit(
            hash = CommitHash(value = hash, abbreviated = abbreviatedHash),
            parents = parents,
            authorName = authorName,
            authorEmail = authorEmail,
            authorDate = Instant.ofEpochSecond(authorDate.toLong()),
            commiterName = commiterName,
            commiterEmail = commiterEmail,
            commiterDate = Instant.ofEpochSecond(commiterDate.toLong()),
            message = message,
            refNames = refNames.map(::RefName),
        )

    private fun VcsChangeType.toChangeType(): ChangeType =
        when (this) {
            VcsChangeType.Add -> ChangeType.Add
            VcsChangeType.Modify -> ChangeType.Modify
            VcsChangeType.Delete -> ChangeType.Delete
            VcsChangeType.Rename -> ChangeType.Rename
            VcsChangeType.Copy -> ChangeType.Copy
        }
}
