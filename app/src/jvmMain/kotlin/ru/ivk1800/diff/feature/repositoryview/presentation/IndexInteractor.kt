package ru.ivk1800.diff.feature.repositoryview.presentation

import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.domain.FileRepository
import ru.ivk1800.diff.feature.repositoryview.domain.IndexRepository
import ru.ivk1800.vcs.git.VcsException
import java.io.File

// TODO: move from presentation folder
class IndexInteractor(
    private val repoDirectory: File,
    private val fileRepository: FileRepository,
    private val diffRepository: DiffRepository,
    private val indexRepository: IndexRepository,
) {

    suspend fun removeFromIndex(fileName: String, hunk: Diff.Hunk): Result<Unit> =
        try {
            Result.success(removeFromIndexInternal(fileName, hunk))
        } catch (error: Throwable) {
            Result.failure(
                // TODO
                VcsException.ParseException(
                    message = "An error occurred while remove hunk from index",
                    cause = error
                )
            )
        }

    private suspend fun removeFromIndexInternal(fileName: String, hunk: Diff.Hunk) {
        val diff: Diff = diffRepository.getStagedFileDiff(repoDirectory, fileName)
        val fileContent: List<Diff.Hunk.Line> = getFileLines(fileName)

        val contentForUnstage = removeHunk(fileContent, diff, hunk)
            .joinToString(separator = System.lineSeparator()) { it.text }
        indexRepository.updateIndex(repoDirectory, fileName = fileName, content = contentForUnstage)
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

    private fun Diff.Hunk.reset(): List<Diff.Hunk.Line> =
        lines.filter { line -> line.type == Diff.Hunk.Line.Type.NotChanged || line.type == Diff.Hunk.Line.Type.Added }

    private suspend fun getFileLines(fileName: String): List<Diff.Hunk.Line> =
        fileRepository.getFileLines(directory = repoDirectory, fileName = fileName)
            .map { line ->
                Diff.Hunk.Line(
                    text = line,
                    type = Diff.Hunk.Line.Type.NotChanged,
                )
            }
}
