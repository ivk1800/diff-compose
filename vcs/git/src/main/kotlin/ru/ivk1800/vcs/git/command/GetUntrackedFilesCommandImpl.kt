package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.GetUntrackedFilesCommand
import ru.ivk1800.vcs.git.util.createProcess
import ru.ivk1800.vcs.git.util.handleResult
import java.nio.file.Path

internal class GetUntrackedFilesCommandImpl(
    private val directory: Path,
) : GetUntrackedFilesCommand() {
    override suspend fun run(): List<String> = withContext(Dispatchers.IO) {
        val process = createProcess(directory, "git ls-files --others --exclude-standard")

        handleResult(
            process,
            onResult = {
                if (it.isBlank()) {
                    emptyList()
                } else {
                    it.trim().split(System.lineSeparator())
                }
            },
            onError = VcsException::ProcessException,
        )
    }
}
