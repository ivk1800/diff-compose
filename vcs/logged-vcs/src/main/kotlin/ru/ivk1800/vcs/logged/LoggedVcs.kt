package ru.ivk1800.vcs.logged

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.ivk1800.vcs.api.Vcs
import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsFile
import ru.ivk1800.vcs.api.VcsStash
import ru.ivk1800.vcs.api.VcsStatus
import ru.ivk1800.vcs.api.command.AddAllToStagedCommand
import ru.ivk1800.vcs.api.command.AddFilesToStagedCommand
import ru.ivk1800.vcs.api.command.Command
import ru.ivk1800.vcs.api.command.DiffCommand
import ru.ivk1800.vcs.api.command.DiscardCommand
import ru.ivk1800.vcs.api.command.GetCommitCommand
import ru.ivk1800.vcs.api.command.GetCommitFilesCommand
import ru.ivk1800.vcs.api.command.GetCommitsCommand
import ru.ivk1800.vcs.api.command.GetStashListCommand
import ru.ivk1800.vcs.api.command.GetUntrackedFilesCommand
import ru.ivk1800.vcs.api.command.HashObjectCommand
import ru.ivk1800.vcs.api.command.RemoveAllFromStagedCommand
import ru.ivk1800.vcs.api.command.RemoveFilesFromStagedCommand
import ru.ivk1800.vcs.api.command.ShowCommand
import ru.ivk1800.vcs.api.command.StatusCommand
import ru.ivk1800.vcs.api.command.UpdateIndexCommand
import java.io.File
import java.nio.file.Path

class LoggedVcs(
    private val target: Vcs,
) : Vcs {

    private val _activeCommands = MutableStateFlow<List<ActiveCommand>>(listOf())
    val activeCommands: Flow<List<ActiveCommand>>
        get() = _activeCommands

    override suspend fun isRepository(directory: File): Boolean {
        return target.isRepository(directory)
    }

    override suspend fun getCommitsCommand(directory: Path, options: GetCommitsCommand.Options): GetCommitsCommand =
        object : GetCommitsCommand {
            override suspend fun run(): List<VcsCommit> =
                wrapResult(
                    name = "getCommitsCommand",
                    target.getCommitsCommand(directory, options),
                )
        }

    override suspend fun getCommitCommand(directory: Path, options: GetCommitCommand.Options): GetCommitCommand =
        object : GetCommitCommand {
            override suspend fun run(): VcsCommit =
                wrapResult(
                    name = "getCommitCommand",
                    target.getCommitCommand(directory, options),
                )
        }

    override suspend fun getCommitFilesCommand(
        directory: Path,
        options: GetCommitFilesCommand.Options,
    ): GetCommitFilesCommand =
        object : GetCommitFilesCommand {
            override suspend fun run(): List<VcsFile> =
                wrapResult(
                    name = "getCommitFilesCommand",
                    target.getCommitFilesCommand(directory, options),
                )
        }

    override suspend fun getDiff(
        directory: File,
        oldCommitHash: String,
        newCommitHash: String,
        filePath: String,
    ): VcsDiff {
        return target.getDiff(directory, oldCommitHash, newCommitHash, filePath)
    }

    override suspend fun getDiff(directory: File, oldBlobId: String, newBlobId: String): VcsDiff {
        return target.getDiff(directory, oldBlobId, newBlobId)
    }

    override suspend fun getDiffCommand(directory: Path, options: DiffCommand.Options): DiffCommand =
        object : DiffCommand {
            override suspend fun run(): VcsDiff {
                return wrapResult(
                    name = "getDiffCommand",
                    target.getDiffCommand(directory, options),
                )
            }
        }

    override suspend fun getUnStagedDiff(directory: File): List<VcsDiff> {
        return target.getUnStagedDiff(directory)
    }

    override suspend fun getStagedDiff(directory: File): List<VcsDiff> {
        return target.getStagedDiff(directory)
    }

    override suspend fun getRemoveAllFromStagedCommand(directory: Path): RemoveAllFromStagedCommand =
        object : RemoveAllFromStagedCommand {
            override suspend fun run() =
                wrapResult(
                    name = "getRemoveAllFromStagedCommand",
                    target.getRemoveAllFromStagedCommand(directory),
                )
        }

    override suspend fun getAddAllToStagedCommand(directory: Path): AddAllToStagedCommand =
        object : AddAllToStagedCommand {
            override suspend fun run() =
                wrapResult(
                    name = "getAddAllToStagedCommand",
                    target.getAddAllToStagedCommand(directory),
                )
        }

    override suspend fun getRemoveFilesFromStagedCommand(
        directory: Path,
        options: RemoveFilesFromStagedCommand.Options,
    ): RemoveFilesFromStagedCommand =
        object : RemoveFilesFromStagedCommand {
            override suspend fun run() =
                wrapResult(
                    name = "getRemoveFilesFromStagedCommand",
                    target.getRemoveFilesFromStagedCommand(directory, options),
                )
        }

    override suspend fun getAddFilesToStagedCommand(
        directory: Path,
        options: AddFilesToStagedCommand.Options,
    ): AddFilesToStagedCommand =
        object : AddFilesToStagedCommand {
            override suspend fun run() =
                wrapResult(
                    name = "getAddFilesToStagedCommand",
                    target.getAddFilesToStagedCommand(directory, options),
                )
        }

    override suspend fun getHashObjectCommand(directory: Path, options: HashObjectCommand.Options): String =
        target.getHashObjectCommand(directory, options)

    override suspend fun getShowCommand(directory: Path, options: ShowCommand.Options): ShowCommand =
        object : ShowCommand {
            override suspend fun run(): List<String> =
                wrapResult(
                    name = "getShowCommand",
                    target.getShowCommand(directory, options),
                )
        }

    override suspend fun getUpdateIndexCommand(
        directory: Path,
        options: UpdateIndexCommand.Options,
    ): UpdateIndexCommand =
        object : UpdateIndexCommand {
            override suspend fun run() =
                wrapResult(
                    name = "getUpdateIndexCommand",
                    target.getUpdateIndexCommand(directory, options),
                )
        }

    override suspend fun getDiscardCommand(directory: Path, options: DiscardCommand.Options): DiscardCommand =
        object : DiscardCommand {
            override suspend fun run() =
                wrapResult(
                    name = "getDiscardCommand",
                    target.getDiscardCommand(directory, options),
                )
        }

    override suspend fun getUntrackedFilesCommand(directory: Path): GetUntrackedFilesCommand =
        object : GetUntrackedFilesCommand {
            override suspend fun run(): List<String> =
                wrapResult(
                    name = "getUntrackedFilesCommand",
                    target.getUntrackedFilesCommand(directory),
                )
        }

    override suspend fun getStatusCommand(directory: Path): StatusCommand =
        object : StatusCommand {
            override suspend fun run(): VcsStatus =
                wrapResult(
                    name = "getCommitsCommand",
                    target.getStatusCommand(directory),
                )
        }

    override suspend fun getStashListCommand(directory: Path): GetStashListCommand =
        object : GetStashListCommand {
            override suspend fun run(): List<VcsStash> =
                wrapResult(
                    name = "getCommitsCommand",
                    target.getStashListCommand(directory),
                )
        }

    private suspend fun <T> wrapResult(
        name: String,
        command: Command<T>,
    ): T {
        val activeCommand = ActiveCommand(name)
        _activeCommands.value = _activeCommands.value.toMutableList().apply { add(activeCommand) }
        return try {
            command.run()
        } finally {
            _activeCommands.value = _activeCommands.value.toMutableList().apply { remove(activeCommand) }
        }
    }

    data class ActiveCommand(val name: String)
}
