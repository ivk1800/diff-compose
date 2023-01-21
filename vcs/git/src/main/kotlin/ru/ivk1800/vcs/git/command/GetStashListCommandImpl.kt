package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.VcsStash
import ru.ivk1800.vcs.api.command.GetStashListCommand
import ru.ivk1800.vcs.git.parser.GitStashListParser
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

internal class GetStashListCommandImpl(
    private val parser: GitStashListParser,
    private val directory: Path,
    private val context: CoroutineContext,
) : BaseCommand(), GetStashListCommand {
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
