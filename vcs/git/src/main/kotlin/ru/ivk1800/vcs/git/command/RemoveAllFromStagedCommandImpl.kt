package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.RemoveAllFromStagedCommand
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

internal class RemoveAllFromStagedCommandImpl(
    private val directory: Path,
    private val context: CoroutineContext,
) : BaseCommand(), RemoveAllFromStagedCommand {
    override suspend fun run() = withContext(context) {
        handleResult(
            createProcess(directory, "git reset HEAD"),
            onError = { error ->
                VcsException.ProcessException(error)
            },
            onResult = { },
        )
    }
}
