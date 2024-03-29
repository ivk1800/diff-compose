package ru.ivk1800.diff.feature.repositoryview.presentation.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ru.ivk1800.diff.feature.repositoryview.domain.ChangeType
import ru.ivk1800.diff.feature.repositoryview.domain.Commit
import ru.ivk1800.diff.feature.repositoryview.domain.CommitFile
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitDescription
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class CommitInfoMapper {
    private val commitDateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss ZZZZZ", Locale("en"))

    fun mapDiffToFiles(files: List<Diff>): ImmutableList<CommitFileItem> =
        files.map { file ->
            CommitFileItem(
                id = CommitFileId(file.filePath),
                name = file.filePath,
                type = file.changeType.toType(),
            )
        }.toImmutableList()

    fun mapToFiles(files: List<CommitFile>): ImmutableList<CommitFileItem> =
        files.map { file ->
            CommitFileItem(
                id = CommitFileId(file.name),
                name = file.name,
                type = file.changeType.toType(),
            )
        }.toImmutableList()

    fun mapToDescription(commit: Commit): CommitDescription {
        return CommitDescription(
            message = commit.message,
            commit = buildString {
                append(commit.hash.value)
                if (commit.hash.abbreviated.isNotEmpty()) {
                    append(" [${commit.hash.abbreviated}]")
                }
            },
            parents = commit.parents.joinToString().takeIf(String::isNotEmpty),
            author = buildString {
                append(commit.authorName)
                if (commit.authorEmail.isNotEmpty()) {
                    append(" <${commit.authorEmail}>")
                }
            },
            date = commitDateFormat.format(ZonedDateTime.ofInstant(commit.authorDate, ZoneId.systemDefault())),
        )
    }

    private fun ChangeType.toType(): CommitFileItem.Type =
        when (this) {
            ChangeType.Add -> CommitFileItem.Type.Added
            ChangeType.Modify -> CommitFileItem.Type.Modified
            ChangeType.Delete -> CommitFileItem.Type.Deleted
            ChangeType.Rename -> CommitFileItem.Type.Renamed
            ChangeType.Copy -> CommitFileItem.Type.Copied
        }
}
