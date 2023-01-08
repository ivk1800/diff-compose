package ru.ivk1800.diff.feature.repositoryview.presentation

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitTableItem
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class SelectionCoordinatorTest {
    @RelaxedMockK
    lateinit var mockCommitsTableInteractor: CommitsTableInteractor

    @RelaxedMockK
    lateinit var mockCommitInfoInteractor: CommitInfoInteractor

    @RelaxedMockK
    lateinit var mockDiffInfoInteractor: DiffInfoInteractor

    @RelaxedMockK
    lateinit var mockUncommittedChangesInteractor: UncommittedChangesInteractor

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should select commits`() {
        sut().selectCommits(persistentSetOf())
        verify { mockCommitsTableInteractor.selectCommits(persistentSetOf()) }
    }

    @Test
    fun `should not display commit info if commit not selected`() = runTest {
        sut {
            commitsTableState = CommitsTableState.Loading
        }.selectCommits(persistentSetOf())
        verify { mockCommitInfoInteractor.selectCommit(null) }
    }

    @Test
    fun `should display commit info if commit is selected`() = runTest {
        sut {
            commitsTableState = CommitsTableState.Content(
                selected = persistentSetOf(CommitTableItem.Id.Commit(CommitId(""))),
                commits = mockk(),
            )
        }.selectCommits(persistentSetOf())
        verify { mockCommitInfoInteractor.selectCommit(CommitId("")) }
    }

    private fun TestScope.sut(init: Sut.() -> Unit = { }): SelectionCoordinator = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private fun sut(init: Sut.() -> Unit = { }): SelectionCoordinator = Sut().apply(init).build()

    private inner class Sut {
        var commitsTableState: CommitsTableState = CommitsTableState.Loading
        var context: CoroutineContext? = null

        fun build(): SelectionCoordinator {
            every { mockCommitsTableInteractor.state } returns MutableStateFlow(commitsTableState)

            return SelectionCoordinator(
                commitsTableInteractor = mockCommitsTableInteractor,
                commitInfoInteractor = mockCommitInfoInteractor,
                diffInfoInteractor = mockDiffInfoInteractor,
                uncommittedChangesInteractor = mockUncommittedChangesInteractor,
                context = requireNotNull(context)
            )
        }
    }
}
