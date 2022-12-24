package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import ru.ivk1800.diff.feature.repositoryview.domain.CommitFile
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitDescription
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem

class CommitInfoMapper {
    fun mapToFiles(files: List<CommitFile>): ImmutableList<CommitFileItem> =
        files.map { file ->
            CommitFileItem(
                name = file.path,
                type = CommitFileItem.Type.Added,
            )
        }.toImmutableList()

    fun mapToDescription(): CommitDescription {
        return CommitDescription(
            message = "Test description",
            commit = "d56b20c63ca1064ca01c67bd89934c56c11b1863 [d56b20c]",
            parents = "22ae08f684",
            author = "Ivan <ivan@ivk1800.ru>",
            date = "24 December 2022, 13:30:35 GMT+4",
        )
    }
}
