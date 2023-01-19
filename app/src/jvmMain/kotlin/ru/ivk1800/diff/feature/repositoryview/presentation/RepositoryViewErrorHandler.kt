package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitsManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.UncommittedChangesManager
import ru.ivk1800.diff.presentation.DialogRouter
import ru.ivk1800.diff.presentation.ErrorTransformer
import kotlin.coroutines.CoroutineContext

class RepositoryViewErrorHandler internal constructor(
    dialogRouter: DialogRouter,
    errorTransformer: ErrorTransformer,
    commitsManager: CommitsManager,
    uncommittedChangesManager: UncommittedChangesManager,
    context: CoroutineContext,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    private val errorsFlow = MutableSharedFlow<Throwable>(extraBufferCapacity = Int.MAX_VALUE)

    init {
        merge(
            errorsFlow,
            uncommittedChangesManager.errors,
            commitsManager.errors,
        )
            .onEach { error ->
                dialogRouter.show(
                    DialogRouter.Dialog.Error(
                        title = "Error",
                        text = errorTransformer.transformForDisplay(error),
                    ),
                )
            }
            .launchIn(scope)
    }

    constructor(
        dialogRouter: DialogRouter,
        errorTransformer: ErrorTransformer,
        commitsManager: CommitsManager,
        uncommittedChangesManager: UncommittedChangesManager,
    ) : this(
        dialogRouter,
        errorTransformer,
        commitsManager,
        uncommittedChangesManager,
        Dispatchers.Main,
    )

    fun processError(error: Throwable) {
        scope.launch { errorsFlow.emit(error) }
    }

    fun dispose() {
        scope.cancel()
    }
}
