package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.UpdateIndexCommand
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.nio.file.Path

internal class UpdateIndexCommandImpl(
    private val directory: Path,
    private val options: UpdateIndexCommand.Options,
) : BaseCommand(), UpdateIndexCommand {
    override suspend fun run() = withContext(Dispatchers.IO) {
        val hashObjectProcess = createProcess(directory, "git hash-object -w --stdin")

        BufferedWriter(OutputStreamWriter(hashObjectProcess.outputStream)).apply {
            write(options.content)
            close()
        }

        val sha1 = handleResult(
            hashObjectProcess,
            onResult = { it.trim() },
            onError = VcsException::ProcessException,
        )

        val process =
            createProcess(
                directory,
                command = "git update-index --add --cacheinfo 100644 $sha1 ${options.fileName}",
            )

        handleResult(
            process,
            onResult = { Unit },
            onError = VcsException::ProcessException,
        )
    }
}
