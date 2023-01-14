package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import kotlin.coroutines.CoroutineContext

// TODO: move from presentation folder
class DiffOperationsInteractor internal constructor(
    private val filesInfoInteractor: FilesInfoInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
    private val changesInteractor: ChangesInteractor,
    context: CoroutineContext,
) {
    constructor(
        filesInfoInteractor: FilesInfoInteractor,
        diffInfoInteractor: DiffInfoInteractor,
        changesInteractor: ChangesInteractor,
    ) : this(
        filesInfoInteractor,
        diffInfoInteractor,
        changesInteractor,
        Dispatchers.IO,
    )

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    suspend fun unstageHunk(hunkId: DiffInfoItem.Id.Hunk): Result<Unit> = runCatching { unstageHunkInternal(hunkId) }

    suspend fun discardHunk(hunkId: DiffInfoItem.Id.Hunk): Result<Unit> = runCatching { discardHunkInternal(hunkId) }

    fun dispose() {
        scope.cancel()
    }

    private suspend fun unstageHunkInternal(hunkId: DiffInfoItem.Id.Hunk) = withContext(scope.coroutineContext) {
        val (fileId, hunk) = runCatching { getHunk(hunkId) }.getOrElse { error ->
            // TODO
            throw IllegalStateException("Unable to unstage hunk", error)
        }

        val result = changesInteractor.removeFromIndex(fileId.path, hunk)
        val error = result.exceptionOrNull()
        if (error != null) {
            throw error
        }
    }

    private suspend fun discardHunkInternal(hunkId: DiffInfoItem.Id.Hunk) = withContext(scope.coroutineContext) {
        val (fileId, hunk) = runCatching { getHunk(hunkId) }.getOrElse { error ->
            // TODO
            throw IllegalStateException("Unable to discard hunk", error)
        }

        val result = changesInteractor.discard(fileId.path, hunk)
        val error = result.exceptionOrNull()
        if (error != null) {
            throw error
        }
    }

    private fun getHunk(hunkId: DiffInfoItem.Id.Hunk): Pair<CommitFileId, Diff.Hunk> {
        val file: CommitFileItem = when (val filesState = filesInfoInteractor.state.value) {
            is FilesInfoState.Commit -> when (filesState.state) {
                is CommitInfoState.Content -> filesState.state.files.first()
                is CommitInfoState.Error,
                CommitInfoState.None -> error("Commit not selected")
            }

            FilesInfoState.None -> error("File not selected")
            is FilesInfoState.UncommittedChanges -> filesState.state.unstaged.files.first()
        }

        check(diffInfoInteractor.state.value is DiffInfoState.Content) { "Diff is not selected" }

        val hunk: Diff.Hunk? = diffInfoInteractor.getHunk(hunkId)
        checkNotNull(hunk) { "Hunk not found" }

        return file.id to hunk
    }
}
