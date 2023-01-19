package ru.ivk1800.vcs.git.command

import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.api.command.GetCommitsCommand
import ru.ivk1800.vcs.git.GitLogOption
import ru.ivk1800.vcs.git.SeparatorBuilder
import ru.ivk1800.vcs.git.VcsException
import ru.ivk1800.vcs.git.parser.GitLogParser
import ru.ivk1800.vcs.git.util.createProcess
import ru.ivk1800.vcs.git.util.handleResult
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext

internal class GetCommitsCommandImpl(
    private val gitLogParser: GitLogParser,
    private val separatorBuilder: SeparatorBuilder,
    private val directory: Path,
    private val context: CoroutineContext,
    options: Options,
) : GetCommitsCommand(options) {
    override suspend fun run(): List<VcsCommit> = withContext(context) {
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

        val afterCommit = options.afterCommit
        val command = if (afterCommit == null) {
            "git log --pretty=format:$pretty -n${options.limit}"
        } else {
            "git log $afterCommit --pretty=format:$pretty -n${options.limit}"
        }
        val process = createProcess(
            directory,
            command = command,
        )

        handleResult(
            process,
            onResult = { raw ->
                val commits = gitLogParser.parseLog(raw)

                if (commits.isNotEmpty() && afterCommit != null) {
                    commits.toMutableList().apply { check(removeAt(0).hash == afterCommit) }
                } else {
                    commits
                }
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
