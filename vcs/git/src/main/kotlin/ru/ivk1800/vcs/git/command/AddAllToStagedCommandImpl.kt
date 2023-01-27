package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.AddAllToStagedCommand
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

internal class AddAllToStagedCommandImpl(
    private val directory: Path,
    private val context: CoroutineContext,
) : BaseCommand(), AddAllToStagedCommand {
    override suspend fun run() = withContext(context) {
        handleResult(
            createProcess(directory, "git add -A"),
            onError = { error ->
                VcsException.ProcessException(error)
            },
            onResult = { },
        )
    }
}
