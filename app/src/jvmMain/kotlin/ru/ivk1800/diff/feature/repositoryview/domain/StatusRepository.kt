package ru.ivk1800.diff.feature.repositoryview.domain

import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsChangeType
import ru.ivk1800.vcs.api.VcsFile
import ru.ivk1800.vcs.api.VcsStatus
import java.io.File

class StatusRepository(
    private val vcs: Vcs,
) {
    suspend fun getStatus(directory: File): Status {
        val status = vcs.getStatusCommand(
            directory.toPath(),
        ).run()
        return toStatus(status)
    }

    private fun toStatus(status: VcsStatus): Status {
        return Status(
            staged = status.staged.map(::toFile),
            unstaged = status.unstaged.map(::toFile),
            untracked = status.untracked.map(::toFile),
        )
    }

    private fun toFile(file: VcsFile): CommitFile =
        CommitFile(
            name = file.name,
            changeType = when (file.changeType) {
                VcsChangeType.Add -> ChangeType.Add
                VcsChangeType.Modify -> ChangeType.Modify
                VcsChangeType.Delete -> ChangeType.Delete
                VcsChangeType.Rename -> ChangeType.Rename
                VcsChangeType.Copy -> ChangeType.Copy
            }
        )
}
