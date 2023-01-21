package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.DiffCommand
import ru.ivk1800.vcs.git.parser.VcsDiffParser
import ru.ivk1800.vcs.git.util.getFileName
import java.nio.file.Path

internal class DiffCommandImpl(
    private val diffParser: VcsDiffParser,
    private val directory: Path,
    private val options: DiffCommand.Options,
) : BaseCommand(), DiffCommand {
    override suspend fun run(): VcsDiff = withContext(Dispatchers.IO) {
        val process = createProcess(
            directory, when (options) {
                is DiffCommand.Options.StagedFile -> "git diff --cached ${options.fileName}"
                is DiffCommand.Options.UnstagedFile -> "git diff ${options.fileName}"
                is DiffCommand.Options.FileInCommit -> "git diff ${options.oldId}..${options.newId}"
            }
        )

        handleResult(
            process,
            onResult = { raw ->
                when (options) {
                    is DiffCommand.Options.FileInCommit -> diffParser.parseMultiple(raw)
                        .firstOrNull { diff -> diff.getFileName() == options.fileName }
                        ?: throw VcsException.ParseException(
                            message = "Unable parse diff of file ${options.fileName}, because it is not in commit.",
                        )

                    is DiffCommand.Options.StagedFile,
                    is DiffCommand.Options.UnstagedFile -> diffParser.parseSingle(raw)
                }
            },
            onError = VcsException::ProcessException,
        )
    }
}
