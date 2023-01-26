package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.VcsFile
import ru.ivk1800.vcs.api.command.GetCommitFilesCommand
import ru.ivk1800.vcs.git.parser.GitShowParser
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

internal class GetCommitFilesCommandImpl(
    private val showParser: GitShowParser,
    private val directory: Path,
    private val options: GetCommitFilesCommand.Options,
    private val context: CoroutineContext,
) : BaseCommand(), GetCommitFilesCommand {
    override suspend fun run(): List<VcsFile> = withContext(context) {
        val process = createProcess(
            directory,
            "git show ${options.hash} --name-status --oneline",
        )

        handleResult(
            process,
            onResult = showParser::parseFiles,
            onError = VcsException::ProcessException,
        )
    }
}
