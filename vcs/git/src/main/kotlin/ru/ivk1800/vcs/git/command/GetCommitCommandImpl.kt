package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.api.VcsException
import ru.ivk1800.vcs.api.command.GetCommitCommand
import ru.ivk1800.vcs.git.GitLogOption
import ru.ivk1800.vcs.git.SeparatorBuilder
import ru.ivk1800.vcs.git.parser.GitLogParser
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

internal class GetCommitCommandImpl(
    private val gitLogParser: GitLogParser,
    private val separatorBuilder: SeparatorBuilder,
    private val directory: Path,
    private val context: CoroutineContext,
    private val options: GetCommitCommand.Options,
) : BaseCommand(), GetCommitCommand {
    override suspend fun run(): VcsCommit = withContext(context) {
        val pretty = buildString {
            append(separatorBuilder.startRecordSeparator())
            append("%n")
            addOption(GitLogOption.Hash)
            addOption(GitLogOption.Parents)
            addOption(GitLogOption.AbbreviatedHash)
            addOption(GitLogOption.RawBody)
            addOption(GitLogOption.AuthorName)
            addOption(GitLogOption.AuthorEmail)
            addOption(GitLogOption.AuthorDate)
            addOption(GitLogOption.CommiterName)
            addOption(GitLogOption.CommiterEmail)
            addOption(GitLogOption.CommiterDate)
            addOption(GitLogOption.RefName)
            append(separatorBuilder.endRecordSeparator())
        }

        val process = createProcess(
            directory,
            "git log -1 --pretty=format:$pretty ${options.hash}",
        )
        handleResult(
            process,
            onResult = { raw ->
                val commits = gitLogParser.parseLog(raw)
                check(commits.size == 1)

                commits[0]
            },
            onError = VcsException::ProcessException,
        )
    }

    private fun StringBuilder.addOption(option: GitLogOption) {
        append("${separatorBuilder.buildStartForOption(option)}%n")
        append("%${option.value}%n")
        append("${separatorBuilder.buildEndForOption(option)}%n")
    }
}
