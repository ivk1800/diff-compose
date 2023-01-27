package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.AddFilesToStagedCommand
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

internal class AddFilesToStagedCommandImpl(
    private val directory: Path,
    private val options: AddFilesToStagedCommand.Options,
    private val context: CoroutineContext,
) : BaseCommand(), AddFilesToStagedCommand {
    override suspend fun run() = withContext(context) {
        handleResult(
            createProcess(directory, "git add ${options.filePaths.joinToString(" ")}"),
            onError = { error ->
                VcsException.ProcessException(error)
            },
            onResult = { },
        )
    }
}
