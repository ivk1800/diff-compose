package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import kotlin.coroutines.CoroutineContext

// TODO: move from presentation folder
class DiffOperationsInteractor internal constructor(
    private val filesInfoInteractor: FilesInfoInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
    private val indexInteractor: IndexInteractor,
    context: CoroutineContext,
) {
    constructor(
        filesInfoInteractor: FilesInfoInteractor,
        diffInfoInteractor: DiffInfoInteractor,
        indexInteractor: IndexInteractor,
    ) : this(
        filesInfoInteractor,
        diffInfoInteractor,
        indexInteractor,
        Dispatchers.IO,
    )

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    suspend fun unstageHunk(hunkId: DiffInfoItem.Id.Hunk): Result<Unit> = runCatching { unstageHunkInternal(hunkId) }

    fun dispose() {
        scope.cancel()
    }

    private suspend fun unstageHunkInternal(hunkId: DiffInfoItem.Id.Hunk) = withContext(scope.coroutineContext) {
        val file = when (val filesState = filesInfoInteractor.state.value) {
            is FilesInfoState.Commit -> when (filesState.state) {
                is CommitInfoState.Content -> filesState.state.files.first()
                is CommitInfoState.Error,
                CommitInfoState.None -> error("TODO")
            }

            FilesInfoState.None -> error("TODO")
            is FilesInfoState.UncommittedChanges -> filesState.state.staged.files.first()
        }

        check(diffInfoInteractor.state.value is DiffInfoState.Content) {
            "Unable to unstage hunk, because diff is not selected"
        }
        val hunk = diffInfoInteractor.getHunk(hunkId)
        checkNotNull(hunk) {
            "Unable to unstage hunk, because hunk not found"
        }

        val result = indexInteractor.removeFromIndex(file.id.path, hunk)
        val error = result.exceptionOrNull()
        if (error != null) {
            throw error
        }
    }
}
