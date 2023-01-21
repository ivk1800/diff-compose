package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.ShowCommand
import java.nio.file.Path

internal class ShowCommandImpl(
    private val directory: Path,
    private val options: ShowCommand.Options,
) : BaseCommand(), ShowCommand {
    override suspend fun run(): List<String> = withContext(Dispatchers.IO) {
        val process = createProcess(directory, "git show HEAD:${options.fileName}")

        handleResult(
            process,
            onResult = { it.split(System.lineSeparator()) },
            onError = VcsException::ProcessException,
        )
    }
}
