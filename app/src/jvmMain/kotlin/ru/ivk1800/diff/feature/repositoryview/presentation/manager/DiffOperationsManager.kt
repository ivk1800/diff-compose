package ru.ivk1800.diff.feature.repositoryview.presentation.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileItem
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.DiffInfoState
import ru.ivk1800.diff.feature.repositoryview.presentation.state.FilesInfoState
import kotlin.coroutines.CoroutineContext

// TODO: move from presentation folder
class DiffOperationsManager internal constructor(
    private val filesInfoManager: FilesInfoManager,
    private val diffInfoManager: DiffInfoManager,
    private val changesManager: ChangesManager,
    context: CoroutineContext,
) {
    constructor(
        filesInfoManager: FilesInfoManager,
        diffInfoManager: DiffInfoManager,
        changesManager: ChangesManager,
    ) : this(
        filesInfoManager,
        diffInfoManager,
        changesManager,
        Dispatchers.IO,
    )

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    suspend fun unstageHunk(hunkId: DiffInfoItem.Id.Hunk): Result<Unit> = runCatching { unstageHunkInternal(hunkId) }

    suspend fun stageHunk(hunkId: DiffInfoItem.Id.Hunk): Result<Unit> = runCatching { stageHunkInternal(hunkId) }

    suspend fun discardHunk(hunkId: DiffInfoItem.Id.Hunk): Result<Unit> = runCatching { discardHunkInternal(hunkId) }

    fun dispose() {
        scope.cancel()
    }

    private suspend fun unstageHunkInternal(hunkId: DiffInfoItem.Id.Hunk) = withContext(scope.coroutineContext) {
        val (fileId, hunk) = runCatching { getHunk(hunkId) }.getOrElse { error ->
            // TODO
            throw IllegalStateException("Unable to unstage hunk", error)
        }

        val result = changesManager.removeFromIndex(fileId.path, hunk)
        val error = result.exceptionOrNull()
        if (error != null) {
            throw error
        }
    }

    private suspend fun stageHunkInternal(hunkId: DiffInfoItem.Id.Hunk) = withContext(scope.coroutineContext) {
        val (fileId, hunk) = runCatching { getHunk(hunkId) }.getOrElse { error ->
            // TODO
            throw IllegalStateException("Unable to stage hunk", error)
        }

        val result = changesManager.addToIndex(fileId.path, hunk)
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

        val result = changesManager.discard(fileId.path, hunk)
        val error = result.exceptionOrNull()
        if (error != null) {
            throw error
        }
    }

    private fun getHunk(hunkId: DiffInfoItem.Id.Hunk): Pair<CommitFileId, Diff.Hunk> {
        val file: CommitFileItem = when (val filesState = filesInfoManager.state.value) {
            is FilesInfoState.Commit -> when (filesState.state) {
                is CommitInfoState.Content -> filesState.state.files.first()
                is CommitInfoState.Error,
                CommitInfoState.None -> error("Commit not selected")
            }

            FilesInfoState.None -> error("File not selected")
            is FilesInfoState.UncommittedChanges -> {
                check(filesState.state.unstaged.selected.size <= 1) {
                    "Only one unstaged file must be selected"
                }
                check(filesState.state.staged.selected.size <= 1) {
                    "Only one staged file must be selected"
                }
                val unstagedSelected = filesState.state.unstaged.selected.firstOrNull()

                if (unstagedSelected != null) {
                    requireNotNull(
                        filesState.state.unstaged.files.firstOrNull { it.id == unstagedSelected },
                    ) {
                        "Could not find unstaged file with path: ${unstagedSelected.path}"
                    }
                } else {
                    val stagedSelected = filesState.state.staged.selected.firstOrNull()

                    if (stagedSelected != null) {
                        requireNotNull(
                            filesState.state.staged.files.firstOrNull { it.id == stagedSelected },
                        ) {
                            "Could not find staged file with path: ${stagedSelected.path}"
                        }
                    } else {
                        error("Unable find file")
                    }
                }
            }
        }

        check(diffInfoManager.state.value is DiffInfoState.Content) { "Diff is not selected" }

        val hunk: Diff.Hunk? = diffInfoManager.getHunk(hunkId)
        checkNotNull(hunk) { "Hunk not found" }

        return file.id to hunk
    }
}
