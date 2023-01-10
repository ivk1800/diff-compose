package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.command.DiffCommand
import ru.ivk1800.vcs.git.VcsException
import ru.ivk1800.vcs.git.parser.VcsDiffParser
import ru.ivk1800.vcs.git.util.createProcess
import ru.ivk1800.vcs.git.util.getFileName
import ru.ivk1800.vcs.git.util.handleResult
import java.nio.file.Path

internal class DiffCommandImpl(
    private val diffParser: VcsDiffParser,
    private val directory: Path,
    options: Options,
) : DiffCommand(options) {
    override suspend fun run(): VcsDiff = withContext(Dispatchers.IO) {
        val process = createProcess(
            directory, when (options) {
                is Options.StagedFile -> "git diff --cached ${options.fileName}"
                is Options.UnstagedFile -> "git diff ${options.fileName}"
                is Options.FileInCommit -> "git diff ${options.oldId}..${options.newId}"
            }
        )

        handleResult(
            process,
            onResult = { raw ->
                when (options) {
                    is Options.FileInCommit -> diffParser.parseMultiple(raw)
                        .firstOrNull { diff -> diff.getFileName() == options.fileName }
                        ?: throw VcsException.ParseException(
                            message = "Unable parse diff of file ${options.fileName}, because it is not in commit.",
                        )

                    is Options.StagedFile,
                    is Options.UnstagedFile -> diffParser.parseSingle(raw)
                }
            },
            onError = VcsException::ProcessException,
        )
    }
}
