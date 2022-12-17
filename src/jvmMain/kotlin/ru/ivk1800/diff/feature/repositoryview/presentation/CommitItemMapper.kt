package ru.ivk1800.diff.feature.repositoryview.presentation

import ru.ivk1800.diff.feature.repositoryview.domain.Commit
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class CommitItemMapper {
    private val commitDateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale("en"))

    fun mapToItem(commit: Commit): CommitItem {
        return CommitItem(
            description = commit.message.trim().lines().first(),
            commit = commit.abbreviatedHash,
            author = "${commit.authorName} <${commit.authorEmail}>",
            date = commitDateFormat.format(ZonedDateTime.ofInstant(commit.authorDate, ZoneId.systemDefault())),
        )
    }
}
