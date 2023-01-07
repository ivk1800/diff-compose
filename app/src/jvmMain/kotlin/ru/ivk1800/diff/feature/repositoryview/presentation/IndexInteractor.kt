package ru.ivk1800.diff.feature.repositoryview.presentation

import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.FileRepository
import ru.ivk1800.diff.feature.repositoryview.domain.IndexRepository
import ru.ivk1800.diff.feature.repositoryview.domain.ObjectRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.ext.getStartLineNumber
import java.io.File

// TODO: move from presentation folder
class IndexInteractor(
    private val repoDirectory: File,
    private val objectRepository: ObjectRepository,
    private val fileRepository: FileRepository,
    private val indexRepository: IndexRepository,
) {

    suspend fun removeFromIndex(fileName: String, hunk: Diff.Hunk, diffId: String) {
        removeFromIndexInternal(fileName, hunk, diffId)
    }

    private suspend fun removeFromIndexInternal(fileName: String, hunk: Diff.Hunk, diffId: String) {
        val startLineNumber = hunk.getStartLineNumber()
        val numberedLines: List<Pair<Int, Diff.Hunk.Line>> = hunk.lines.mapIndexed { index, line ->
            startLineNumber + index to line
        }
        val linesForRemove =
            numberedLines.filter {
                it.second.type == Diff.Hunk.Line.Type.Removed ||
                        it.second.type == Diff.Hunk.Line.Type.Added
            }

        val fileContent = fileRepository.getFileLines(directory = repoDirectory, diffId = diffId)
        val contentForUnstage = fileContent.toMutableList().apply {
            linesForRemove.forEach { line ->
                removeAt(line.first - 1)
            }
        }.joinToString(separator = System.lineSeparator())
        val objectId = objectRepository.writeToDatabase(repoDirectory, contentForUnstage)

        indexRepository.updateIndex(repoDirectory, fileName = fileName, id = objectId)
    }
}
