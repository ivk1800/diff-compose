package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.presentation.event.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.event.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.event.SidePanelEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import ru.ivk1800.diff.feature.repositoryview.presentation.workspace.WorkspaceInteractor
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
    private val workspaceInteractor: WorkspaceInteractor,
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
        workspaceInteractor: WorkspaceInteractor
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
        workspaceInteractor,
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
        }
    }

    fun onSidePanelEvent(value: SidePanelEvent) {
        when (value) {
            is SidePanelEvent.OnSectionUnselected -> workspaceInteractor.selectSection(value.value)
        }
    }

    fun onHistoryEvent(value: HistoryEvent) {
        when (value) {

            is HistoryEvent.OnCommitsSelected -> {
                selectionCoordinator.selectCommits(value.items)
            }

            HistoryEvent.OnCommitsUnselected -> commitInfoInteractor.selectCommit(null)
            is HistoryEvent.OnFilesSelected ->
                selectionCoordinator.selectCommitFiles(value.items)

            HistoryEvent.OnLoadMoreCommits -> commitsTableInteractor.loadMore()
            HistoryEvent.OnUncommittedChangesSelected -> selectionCoordinator.selectUncommittedChanges()

            is HistoryEvent.UncommittedChanges ->
                when (value) {
                    HistoryEvent.UncommittedChanges.OnAddAllToStaged ->
                        uncommittedChangesInteractor.addAllToStaged()

                    HistoryEvent.UncommittedChanges.OnRemoveAllFromStaged ->
                        uncommittedChangesInteractor.removeAllFromStaged()

                    is HistoryEvent.UncommittedChanges.OnAddFilesToStaged ->
                        uncommittedChangesInteractor.addFilesToStaged(value.ids)

                    is HistoryEvent.UncommittedChanges.OnRemoveFilesFromStaged ->
                        uncommittedChangesInteractor.removeFilesFromStaged(value.ids)

                    is HistoryEvent.UncommittedChanges.OnStatedFilesSelected ->
                        selectionCoordinator.selectStatedFiles(value.items)

                    is HistoryEvent.UncommittedChanges.OnUnstatedFilesSelected ->
                        selectionCoordinator.selectUnstatedFiles(value.items)
                }

            is HistoryEvent.Diff -> {
                when (value) {
                    is HistoryEvent.Diff.OnLinesSelected ->
                        selectionCoordinator.selectDiffLines(value.ids)

                    is HistoryEvent.Diff.OnUnstageHunk -> onUnstageHunk(value)
                    is HistoryEvent.Diff.OnDiscardHunk -> onDiscardHunk(value)
                }
            }
        }
    }

    fun dispose() {
        scope.cancel()
    }

    private fun onUnstageHunk(event: HistoryEvent.Diff.OnUnstageHunk) {
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

    private fun onDiscardHunk(event: HistoryEvent.Diff.OnDiscardHunk) {
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
