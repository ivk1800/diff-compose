package ru.ivk1800.vcs.git.parser

import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.git.GitLogOption
import ru.ivk1800.vcs.git.SeparatorBuilder

internal class GitLogParser(
    private val separatorBuilder: SeparatorBuilder,
) {
    private val recordRegex =
        "${separatorBuilder.startRecordSeparator()}((.|\\n)*?)${separatorBuilder.endRecordSeparator()}".toRegex()

    fun parseLog(raw: String): List<VcsCommit> = toCommitRecords(raw).map(::parseCommitRecord).toList()

    private fun toCommitRecords(raw: String): Sequence<String> =
        recordRegex.findAll(raw)
            .map { requireNotNull(it.groups[1]).value.trim() }

    private fun parseCommitRecord(raw: String): VcsCommit =
        VcsCommit(
            hash = raw.between(GitLogOption.Hash),
            parents = raw.between(GitLogOption.Parents).split(" "),
            abbreviatedHash = raw.between(GitLogOption.AbbreviatedHash),
            authorName = raw.between(GitLogOption.AuthorName),
            authorEmail = raw.between(GitLogOption.AuthorEmail),
            authorDate = raw.between(GitLogOption.AuthorDate).toInt(),
            commiterName = raw.between(GitLogOption.CommiterName),
            commiterEmail = raw.between(GitLogOption.CommiterEmail),
            commiterDate = raw.between(GitLogOption.CommiterDate).toInt(),
            message = raw.between(GitLogOption.RawBody),
            refNames = raw.between(GitLogOption.RefName).takeIf { it.isNotEmpty() }?.split(", ").orEmpty(),
        )

    private fun String.between(option: GitLogOption): String =
        substringAfter("${separatorBuilder.buildStartForOption(option)}\n", missingDelimiterValue = "")
            .substringBefore("\n${separatorBuilder.buildEndForOption(option)}", missingDelimiterValue = "")
}