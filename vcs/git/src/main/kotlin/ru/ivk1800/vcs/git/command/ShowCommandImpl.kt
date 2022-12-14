package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.command.ShowCommand
import ru.ivk1800.vcs.git.VcsException
import ru.ivk1800.vcs.git.util.createProcess
import ru.ivk1800.vcs.git.util.handleResult
import java.nio.file.Path

internal class ShowCommandImpl(private val directory: Path, options: Options) : ShowCommand(options) {
    override suspend fun run(): List<String> = withContext(Dispatchers.IO) {
        val process = createProcess(directory, "git show ${options.objectId}")

        handleResult(
            process,
            onResult = { it.split(System.lineSeparator()) },
            onError = VcsException::ProcessException,
        )
    }
}
