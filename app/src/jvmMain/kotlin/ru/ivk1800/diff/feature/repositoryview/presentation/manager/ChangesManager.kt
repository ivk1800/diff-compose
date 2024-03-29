package ru.ivk1800.diff.feature.repositoryview.presentation.manager

import ru.ivk1800.diff.exception.PerformActionException
import ru.ivk1800.diff.feature.repositoryview.domain.ChangesRepository
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.domain.FileRepository

// TODO: move from presentation folder
class ChangesManager(
    private val fileRepository: FileRepository,
    private val diffRepository: DiffRepository,
    private val changesRepository: ChangesRepository,
) {

    suspend fun removeFromIndex(fileName: String, hunk: Diff.Hunk): Result<Unit> =
        try {
            Result.success(removeFromIndexInternal(fileName, hunk))
        } catch (error: Throwable) {
            Result.failure(
                PerformActionException(
                    message = "An error occurred while remove hunk from index",
                    cause = error,
                )
            )
        }

    suspend fun addToIndex(fileName: String, hunk: Diff.Hunk): Result<Unit> =
        try {
            Result.success(addToIndexInternal(fileName, hunk))
        } catch (error: Throwable) {
            Result.failure(
                PerformActionException(
                    message = "An error occurred while add hunk to index",
                    cause = error,
                )
            )
        }

    suspend fun discard(fileName: String, hunk: Diff.Hunk): Result<Unit> =
        try {
            Result.success(discardInternal(fileName, hunk))
        } catch (error: Throwable) {
            Result.failure(
                PerformActionException(
                    message = "An error occurred while discard hunk",
                    cause = error,
                )
            )
        }

    private suspend fun discardInternal(fileName: String, hunk: Diff.Hunk) {
        val diff: Diff = diffRepository.getUnstagedFileDiff(fileName)
        val fileContent: List<Diff.Hunk.Line> = getFileLines(fileName)

        val actualContent = removeHunk(fileContent, diff, hunk)
            .joinToString(separator = System.lineSeparator()) { it.text }
        changesRepository.discard(fileName = fileName, content = actualContent)
    }

    private suspend fun removeFromIndexInternal(fileName: String, hunk: Diff.Hunk) {
        val diff: Diff = diffRepository.getStagedFileDiff(fileName)
        val fileContent: List<Diff.Hunk.Line> = getFileLines(fileName)

        val contentForUnstage = removeHunk(fileContent, diff, hunk)
            .joinToString(separator = System.lineSeparator()) { it.text }
        changesRepository.updateIndex(fileName = fileName, content = contentForUnstage)
    }

    private suspend fun addToIndexInternal(fileName: String, hunk: Diff.Hunk) {
        val diff: Diff = diffRepository.getUnstagedFileDiff(fileName)
        val fileContent: List<Diff.Hunk.Line> = getFileLines(fileName)

        val contentForStage = addHunk(fileContent, diff, hunk)
            .joinToString(separator = System.lineSeparator()) { it.text }
        changesRepository.updateIndex(fileName = fileName, content = contentForStage)
    }

    private fun removeHunk(
        fileContent: List<Diff.Hunk.Line>,
        diff: Diff,
        hunkForRemove: Diff.Hunk,
    ): List<Diff.Hunk.Line> {
        var fileLines: MutableList<Diff.Hunk.Line> = fileContent.toMutableList()
        diff.hunks.reversed()
            .filter { hunk -> hunkForRemove != hunk }
            .forEach { hunk ->
                val start = hunk.firstRange.first
                val end = hunk.firstRange.last
                val before = fileLines.subList(0, start - 1)
                val after = fileLines.subList(end, fileLines.size)

                fileLines = (before + hunk.reset() + after).toMutableList()
            }

        return fileLines
    }

    private fun addHunk(
        fileContent: List<Diff.Hunk.Line>,
        diff: Diff,
        hunkForRemove: Diff.Hunk,
    ): List<Diff.Hunk.Line> {
        var fileLines: MutableList<Diff.Hunk.Line> = fileContent.toMutableList()
        diff.hunks.reversed()
            .filter { hunk -> hunkForRemove == hunk }
            .forEach { hunk ->
                val start = hunk.firstRange.first
                val end = hunk.firstRange.last
                val before = fileLines.subList(0, start - 1)
                val after = fileLines.subList(end, fileLines.size)

                fileLines = (before + hunk.reset() + after).toMutableList()
            }

        return fileLines
    }

    private fun Diff.Hunk.reset(): List<Diff.Hunk.Line> =
        lines.filter { line -> line.type == Diff.Hunk.Line.Type.NotChanged || line.type == Diff.Hunk.Line.Type.Added }

    private suspend fun getFileLines(fileName: String): List<Diff.Hunk.Line> =
        fileRepository.getFileLines(fileName = fileName)
            .map { line ->
                Diff.Hunk.Line(
                    text = line,
                    type = Diff.Hunk.Line.Type.NotChanged,
                )
            }
}
