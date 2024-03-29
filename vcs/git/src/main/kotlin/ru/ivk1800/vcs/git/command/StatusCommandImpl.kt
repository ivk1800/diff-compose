package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.VcsStatus
import ru.ivk1800.vcs.api.command.StatusCommand
import ru.ivk1800.vcs.git.parser.GitStatusParser
import java.nio.file.Path

internal class StatusCommandImpl(
    private val directory: Path,
    private val parser: GitStatusParser,
) : BaseCommand(), StatusCommand {
    override suspend fun run(): VcsStatus = withContext(Dispatchers.IO) {
        val process = createProcess(directory, "git status")

        handleResult(
            process,
            onResult = parser::parse,
            onError = VcsException::ProcessException,
        )
    }
}
