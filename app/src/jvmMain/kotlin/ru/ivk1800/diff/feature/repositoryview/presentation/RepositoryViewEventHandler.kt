package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import ru.ivk1800.diff.presentation.DialogRouter
import ru.ivk1800.diff.presentation.ErrorTransformer
import java.io.File
import kotlin.coroutines.CoroutineContext

class RepositoryViewEventHandler internal constructor(
    private val repositoryDirectory: File,
    private val dialogRouter: DialogRouter,
    private val diffOperationsInteractor: DiffOperationsInteractor,
    private val errorTransformer: ErrorTransformer,
    private val commitInfoInteractor: CommitInfoInteractor,
    private val commitsTableInteractor: CommitsTableInteractor,
    private val selectionCoordinator: SelectionCoordinator,
    private val router: RepositoryViewRouter,
    private val uncommittedChangesInteractor: UncommittedChangesInteractor,
    private val diffInfoInteractor: DiffInfoInteractor,
    context: CoroutineContext,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    constructor(
        repositoryDirectory: File,
        dialogRouter: DialogRouter,
        diffOperationsInteractor: DiffOperationsInteractor,
        errorTransformer: ErrorTransformer,
        commitInfoInteractor: CommitInfoInteractor,
        commitsTableInteractor: CommitsTableInteractor,
        selectionCoordinator: SelectionCoordinator,
        router: RepositoryViewRouter,
        uncommittedChangesInteractor: UncommittedChangesInteractor,
        diffInfoInteractor: DiffInfoInteractor,
    ) : this(
        repositoryDirectory,
        dialogRouter,
        diffOperationsInteractor,
        errorTransformer,
        commitInfoInteractor,
        commitsTableInteractor,
        selectionCoordinator,
        router,
        uncommittedChangesInteractor,
        diffInfoInteractor,
        Dispatchers.Main,
    )

    fun onEvent(value: RepositoryViewEvent) {
        when (value) {
            RepositoryViewEvent.OnReload -> {
                commitInfoInteractor.selectCommit(null)
                diffInfoInteractor.unselect()
                commitsTableInteractor.reload()
                uncommittedChangesInteractor.check()
            }

            RepositoryViewEvent.OpenTerminal -> router.toTerminal(repositoryDirectory)
            RepositoryViewEvent.OpenFinder -> router.toFinder(repositoryDirectory)
            is RepositoryViewEvent.OnCommitsSelected -> {
                selectionCoordinator.selectCommits(value.items)
            }

            RepositoryViewEvent.OnCommitsUnselected -> commitInfoInteractor.selectCommit(null)
            is RepositoryViewEvent.OnFilesSelected ->
                selectionCoordinator.selectCommitFiles(value.items)

            RepositoryViewEvent.OnLoadMoreCommits -> commitsTableInteractor.loadMore()
            RepositoryViewEvent.OnUncommittedChangesSelected -> selectionCoordinator.selectUncommittedChanges()

            is RepositoryViewEvent.UncommittedChanges ->
                when (value) {
                    RepositoryViewEvent.UncommittedChanges.OnAddAllToStaged ->
                        uncommittedChangesInteractor.addAllToStaged()

                    RepositoryViewEvent.UncommittedChanges.OnRemoveAllFromStaged ->
                        uncommittedChangesInteractor.removeAllFromStaged()

                    is RepositoryViewEvent.UncommittedChanges.OnAddFilesToStaged ->
                        uncommittedChangesInteractor.addFilesToStaged(value.ids)

                    is RepositoryViewEvent.UncommittedChanges.OnRemoveFilesFromStaged ->
                        uncommittedChangesInteractor.removeFilesFromStaged(value.ids)

                    is RepositoryViewEvent.UncommittedChanges.OnStatedFilesSelected ->
                        selectionCoordinator.selectStatedFiles(value.items)

                    is RepositoryViewEvent.UncommittedChanges.OnUnstatedFilesSelected ->
                        selectionCoordinator.selectUnstatedFiles(value.items)
                }

            is RepositoryViewEvent.Diff -> {
                when (value) {
                    is RepositoryViewEvent.Diff.OnLinesSelected ->
                        selectionCoordinator.selectDiffLines(value.ids)

                    is RepositoryViewEvent.Diff.OnUnstageHunk -> onUnstageHunk(value)
                    is RepositoryViewEvent.Diff.OnDiscardHunk -> onDiscardHunk(value)
                }
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }

    private fun onUnstageHunk(event: RepositoryViewEvent.Diff.OnUnstageHunk) {
        scope.launch {
            val result = diffOperationsInteractor.unstageHunk(event.hunkId)
            val error = result.exceptionOrNull()
            if (error != null) {
                dialogRouter.show(
                    DialogRouter.Dialog.Error(
                        title = "Error",
                        text = errorTransformer.transformForDisplay(error),
                    )
                )
            } else {
                uncommittedChangesInteractor.check()
                diffInfoInteractor.refresh()
            }
        }
    }

    private fun onDiscardHunk(event: RepositoryViewEvent.Diff.OnDiscardHunk) {
        dialogRouter.show(
            DialogRouter.Dialog.Confirmation(
                title = "Confirm discard changes?",
                text = "Are you sure you want to discard all the changes in this hunk?",
                positiveText = "OK",
                negativeText = "Cancel",
                positiveCallback = {
                    proceedDiscardHunk(event.hunkId)
                },
            )
        )
    }

    private fun proceedDiscardHunk(hunkId: DiffInfoItem.Id.Hunk) {
        scope.launch {
            val result = diffOperationsInteractor.discardHunk(hunkId)
            val error = result.exceptionOrNull()
            if (error != null) {
                dialogRouter.show(
                    DialogRouter.Dialog.Error(
                        title = "Error",
                        text = errorTransformer.transformForDisplay(error),
                    )
                )
            } else {
                uncommittedChangesInteractor.check()
                diffInfoInteractor.refresh()
            }
        }
    }
}
