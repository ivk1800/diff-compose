package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.command.UpdateIndexCommand
import ru.ivk1800.vcs.git.VcsException
import ru.ivk1800.vcs.git.util.createProcess
import ru.ivk1800.vcs.git.util.handleResult
import java.nio.file.Path

class UpdateIndexCommandImpl(private val directory: Path, options: Options) : UpdateIndexCommand(options) {
    override suspend fun run() = withContext(Dispatchers.IO) {
        val process =
            createProcess(
                directory,
                command = "git update-index --add --cacheinfo 100644 ${options.sha1} ${options.fileName}",
            )

        handleResult(
            process,
            onResult = { Unit },
            onError = VcsException::ProcessException,
        )
    }
}
