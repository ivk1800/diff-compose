package ru.ivk1800.diff.feature.repositoryview.presentation

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.CommitsTableManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.DiffInfoManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.DiffOperationsManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.UncommittedChangesManager
import ru.ivk1800.diff.feature.repositoryview.presentation.manager.WorkspaceManager
import ru.ivk1800.diff.presentation.DialogRouter
import ru.ivk1800.diff.presentation.ErrorTransformer
import java.io.File
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryViewEventHandlerTest {

    @RelaxedMockK
    lateinit var mockDialogRouter: DialogRouter

    @RelaxedMockK
    lateinit var mockDiffOperationsManager: DiffOperationsManager

    @RelaxedMockK
    lateinit var mockErrorTransformer: ErrorTransformer

    @RelaxedMockK
    lateinit var mockCommitInfoManager: CommitInfoManager

    @RelaxedMockK
    lateinit var mockCommitsTableManager: CommitsTableManager

    @RelaxedMockK
    lateinit var mockSelectionCoordinator: SelectionCoordinator

    @RelaxedMockK
    lateinit var mockRepositoryViewRouter: RepositoryViewRouter

    @RelaxedMockK
    lateinit var mockUncommittedChangesManager: UncommittedChangesManager

    @RelaxedMockK
    lateinit var mockDiffInfoManager: DiffInfoManager

    @RelaxedMockK
    lateinit var mockWorkspaceManager: WorkspaceManager

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    private fun TestScope.sut(init: Sut.() -> Unit = { }): RepositoryViewEventHandler = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private inner class Sut {
        var context: CoroutineContext? = null

        fun build(): RepositoryViewEventHandler {
            return RepositoryViewEventHandler(
                repositoryDirectory = File(""),
                dialogRouter = mockDialogRouter,
                diffOperationsManager = mockDiffOperationsManager,
                errorTransformer = mockErrorTransformer,
                commitInfoManager = mockCommitInfoManager,
                commitsTableManager = mockCommitsTableManager,
                selectionCoordinator = mockSelectionCoordinator,
                router = mockRepositoryViewRouter,
                uncommittedChangesManager = mockUncommittedChangesManager,
                diffInfoManager = mockDiffInfoManager,
                workspaceManager = mockWorkspaceManager,
                context = requireNotNull(context),
            )
        }
    }
}
