package ru.ivk1800.diff.feature.repositoryview.presentation

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import ru.ivk1800.diff.presentation.DialogRouter
import ru.ivk1800.diff.presentation.ErrorTransformer
import java.io.File
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryViewEventHandlerTest {

    @RelaxedMockK
    lateinit var mockDialogRouter: DialogRouter

    @RelaxedMockK
    lateinit var mockDiffOperationsInteractor: DiffOperationsInteractor

    @RelaxedMockK
    lateinit var mockErrorTransformer: ErrorTransformer

    @RelaxedMockK
    lateinit var mockCommitInfoInteractor: CommitInfoInteractor

    @RelaxedMockK
    lateinit var mockCommitsTableInteractor: CommitsTableInteractor

    @RelaxedMockK
    lateinit var mockSelectionCoordinator: SelectionCoordinator

    @RelaxedMockK
    lateinit var mockRepositoryViewRouter: RepositoryViewRouter

    @RelaxedMockK
    lateinit var mockUncommittedChangesInteractor: UncommittedChangesInteractor

    @RelaxedMockK
    lateinit var mockDiffInfoInteractor: DiffInfoInteractor

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
                diffOperationsInteractor = mockDiffOperationsInteractor,
                errorTransformer = mockErrorTransformer,
                commitInfoInteractor = mockCommitInfoInteractor,
                commitsTableInteractor = mockCommitsTableInteractor,
                selectionCoordinator = mockSelectionCoordinator,
                router = mockRepositoryViewRouter,
                uncommittedChangesInteractor = mockUncommittedChangesInteractor,
                diffInfoInteractor = mockDiffInfoInteractor,
                context = requireNotNull(context),
            )
        }
    }
}
