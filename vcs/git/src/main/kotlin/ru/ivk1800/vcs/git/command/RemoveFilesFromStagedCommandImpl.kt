package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.AddAllToStagedCommand
import ru.ivk1800.vcs.api.command.RemoveFilesFromStagedCommand
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

internal class RemoveFilesFromStagedCommandImpl(
    private val directory: Path,
    private val options: RemoveFilesFromStagedCommand.Options,
    private val context: CoroutineContext,
) : BaseCommand(), RemoveFilesFromStagedCommand {
    override suspend fun run() = withContext(context) {
        handleResult(
            createProcess(directory, "git reset HEAD ${options.filePaths.joinToString(" ")}"),
            onError = { error ->
                VcsException.ProcessException(error)
            },
            onResult = { },
        )
    }
}
