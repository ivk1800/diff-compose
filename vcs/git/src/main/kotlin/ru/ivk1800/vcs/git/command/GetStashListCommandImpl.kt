package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.api.VcsStash
import ru.ivk1800.vcs.api.command.GetCommitsCommand
import ru.ivk1800.vcs.api.command.GetStashListCommand
import ru.ivk1800.vcs.git.GitLogOption
import ru.ivk1800.vcs.git.SeparatorBuilder
import ru.ivk1800.vcs.git.VcsException
import ru.ivk1800.vcs.git.parser.GitLogParser
import ru.ivk1800.vcs.git.parser.GitStashListParser
import ru.ivk1800.vcs.git.util.createProcess
import ru.ivk1800.vcs.git.util.handleResult
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

internal class GetStashListCommandImpl(
    private val parser: GitStashListParser,
    private val directory: Path,
    private val context: CoroutineContext,
) : GetStashListCommand {
    override suspend fun run(): List<VcsStash> = withContext(context) {
        val process = createProcess(
            directory,
            command = "git stash list",
        )

        handleResult(
            process,
            onResult = parser::parse,
            onError = VcsException::ProcessException,
        )
    }
}
