package ru.ivk1800.diff.feature.repositoryview.presentation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.ivk1800.diff.feature.repositoryview.presentation.event.HistoryEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.event.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.event.SidePanelEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitsTableManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.DiffInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.DiffOperationsManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.UncommittedChangesManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.WorkspaceManager
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import ru.ivk1800.diff.logging.Logger
import ru.ivk1800.diff.presentation.DialogRouter
import java.io.File
import kotlin.coroutines.CoroutineContext

class RepositoryViewEventHandler internal constructor(
    private val repositoryDirectory: File,
    private val dialogRouter: DialogRouter,
    private val diffOperationsManager: DiffOperationsManager,
    private val commitInfoManager: CommitInfoManager,
    private val commitsTableManager: CommitsTableManager,
    private val selectionCoordinator: SelectionCoordinator,
    private val router: RepositoryViewRouter,
    private val uncommittedChangesManager: UncommittedChangesManager,
    private val diffInfoManager: DiffInfoManager,
    private val workspaceManager: WorkspaceManager,
    private val errorHandler: RepositoryViewErrorHandler,
    private val logger: Logger,
    context: CoroutineContext,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + context)

    constructor(
        repositoryDirectory: File,
        dialogRouter: DialogRouter,
        diffOperationsManager: DiffOperationsManager,
        commitInfoManager: CommitInfoManager,
        commitsTableManager: CommitsTableManager,
        selectionCoordinator: SelectionCoordinator,
        router: RepositoryViewRouter,
        uncommittedChangesManager: UncommittedChangesManager,
        diffInfoManager: DiffInfoManager,
        workspaceManager: WorkspaceManager,
        errorHandler: RepositoryViewErrorHandler,
        logger: Logger,
    ) : this(
        repositoryDirectory,
        dialogRouter,
        diffOperationsManager,
        commitInfoManager,
        commitsTableManager,
        selectionCoordinator,
        router,
        uncommittedChangesManager,
        diffInfoManager,
        workspaceManager,
        errorHandler,
        logger,
        Dispatchers.Main,
    )

    fun onEvent(value: RepositoryViewEvent) {
        logger.d(tag = Tag, message = "Handle event: $value")
        when (value) {
            RepositoryViewEvent.OnReload -> {
                commitInfoManager.selectCommit(null)
                diffInfoManager.unselect()
                commitsTableManager.reload()
                uncommittedChangesManager.check()
            }

            RepositoryViewEvent.OpenTerminal -> router.toTerminal(repositoryDirectory)
            RepositoryViewEvent.OpenFinder -> router.toFinder(repositoryDirectory)
        }
    }

    fun onSidePanelEvent(value: SidePanelEvent) {
        logger.d(tag = Tag, message = "Handle side panel event: $value")
        when (value) {
            is SidePanelEvent.OnSectionUnselected -> workspaceManager.selectSection(value.value)
        }
    }

    fun onHistoryEvent(value: HistoryEvent) {
        logger.d(tag = Tag, message = "Handle history event: $value")
        when (value) {

            is HistoryEvent.OnCommitsSelected -> {
                selectionCoordinator.selectCommits(value.items)
            }

            HistoryEvent.OnCommitsUnselected -> commitInfoManager.selectCommit(null)
            is HistoryEvent.OnFilesSelected ->
                selectionCoordinator.selectCommitFiles(value.items)

            HistoryEvent.OnLoadMoreCommits -> commitsTableManager.loadMore()
            HistoryEvent.OnUncommittedChangesSelected -> selectionCoordinator.selectUncommittedChanges()

            is HistoryEvent.UncommittedChanges ->
                when (value) {
                    HistoryEvent.UncommittedChanges.OnAddAllToStaged ->
                        uncommittedChangesManager.addAllToStaged()

                    HistoryEvent.UncommittedChanges.OnRemoveAllFromStaged ->
                        uncommittedChangesManager.removeAllFromStaged()

                    is HistoryEvent.UncommittedChanges.OnAddFilesToStaged ->
                        uncommittedChangesManager.addFilesToStaged(value.ids)

                    is HistoryEvent.UncommittedChanges.OnRemoveFilesFromStaged ->
                        uncommittedChangesManager.removeFilesFromStaged(value.ids)

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
                    is HistoryEvent.Diff.OnStageHunk -> onStageHunk(value)
                }
            }
        }
    }

    fun dispose() {
        logger.d(tag = Tag, message = "dispose")
        scope.cancel()
    }

    private fun onUnstageHunk(event: HistoryEvent.Diff.OnUnstageHunk) {
        scope.launch {
            val result = diffOperationsManager.unstageHunk(event.hunkId)
            val error = result.exceptionOrNull()
            if (error != null) {
                errorHandler.processError(error)
            } else {
                uncommittedChangesManager.check()
                diffInfoManager.refresh()
            }
        }
    }

    private fun onStageHunk(event: HistoryEvent.Diff.OnStageHunk) {
        scope.launch {
            val result = diffOperationsManager.stageHunk(event.hunkId)
            val error = result.exceptionOrNull()
            if (error != null) {
                errorHandler.processError(error)
            } else {
                uncommittedChangesManager.check()
                diffInfoManager.refresh()
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
            val result = diffOperationsManager.discardHunk(hunkId)
            val error = result.exceptionOrNull()
            if (error != null) {
                errorHandler.processError(error)
            } else {
                uncommittedChangesManager.check()
                diffInfoManager.refresh()
            }
        }
    }

    private companion object {
        private val Tag = RepositoryViewEventHandler::class.simpleName.orEmpty()
    }
}
