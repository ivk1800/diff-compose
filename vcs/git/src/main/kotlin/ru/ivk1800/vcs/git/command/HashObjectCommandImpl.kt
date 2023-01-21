package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.HashObjectCommand
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.nio.file.Path

class HashObjectCommandImpl(
    private val directory: Path,
    private val options: HashObjectCommand.Options,
) : BaseCommand(), HashObjectCommand {
    override suspend fun run(): String = withContext(Dispatchers.IO) {
        val process = createProcess(directory, "git hash-object -w --stdin")

        BufferedWriter(OutputStreamWriter(process.outputStream)).apply {
            write(options.content)
            close()
        }

        handleResult(
            process,
            onResult = { it.trim() },
            onError = VcsException::ProcessException,
        )
    }
}
