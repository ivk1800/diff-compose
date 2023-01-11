package ru.ivk1800.diff.feature.repositoryview.presentation

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
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
    fun `should select commits`() = runTest {
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

    // region commits selection

    @Test
    fun `should reset selected files if select nothing`() = runTest {
        sut().selectCommitFiles(persistentSetOf())
        verify { mockCommitInfoInteractor.selectFiles(persistentSetOf()) }
    }

    @Test
    fun `should display diff of selected file`() = runTest {
        sut {
            commitInfoState = CommitInfoState.Content(
                selected = persistentSetOf(
                    CommitFileId("")
                ),
                files = persistentListOf(),
                description = mockk(),
            )
        }
        verify { mockDiffInfoInteractor.onFileSelected(any(), any()) }
    }

    @Test
    fun `should not display diff of file if not selected`() = runTest {
        sut {
            commitInfoState = CommitInfoState.Content(
                selected = persistentSetOf(),
                files = persistentListOf(),
                description = mockk(),
            )
        }
        verify { mockDiffInfoInteractor.unselect() }
    }

    // endregion commits selection

    // region diff selection

    @Test
    fun `should not display diff if uncommitted files not selected`() = runTest {
        sut {
            uncommittedChangesState = UncommittedChangesState.None
        }
        verify { mockDiffInfoInteractor.unselect() }
    }

    @Test
    fun `should display diff if selected staged uncommitted files`() = runTest {
        sut {
            uncommittedChangesState = UncommittedChangesState.Content(
                staged = UncommittedChangesState.Content.Staged(
                    selected = persistentSetOf(
                        CommitFileId(""),
                    ),
                    vcsProcess = false,
                    files = persistentListOf(),
                ),
                unstaged = UncommittedChangesState.Content.Unstaged(
                    selected = persistentSetOf(),
                    vcsProcess = false,
                    files = persistentListOf(),
                ),
            )
        }
        verify {
            mockDiffInfoInteractor.selectUncommittedFiles(
                fileName = "",
                type = DiffInfoInteractor.UncommittedChangesType.Staged,
            )
        }
    }

    @Test
    fun `should display diff if selected unstaged uncommitted files`() = runTest {
        sut {
            uncommittedChangesState = UncommittedChangesState.Content(
                staged = UncommittedChangesState.Content.Staged(
                    selected = persistentSetOf(),
                    vcsProcess = false,
                    files = persistentListOf(),
                ),
                unstaged = UncommittedChangesState.Content.Unstaged(
                    selected = persistentSetOf(
                        CommitFileId(""),
                    ),
                    vcsProcess = false,
                    files = persistentListOf(),
                ),
            )
        }
        verify {
            mockDiffInfoInteractor.selectUncommittedFiles(
                fileName = "",
                type = DiffInfoInteractor.UncommittedChangesType.Unstaged,
            )
        }
    }

    @Test
    fun `should not display diff if nothing selected`() = runTest {
        sut {
            uncommittedChangesState = UncommittedChangesState.Content(
                staged = UncommittedChangesState.Content.Staged(
                    selected = persistentSetOf(),
                    vcsProcess = false,
                    files = persistentListOf(),
                ),
                unstaged = UncommittedChangesState.Content.Unstaged(
                    selected = persistentSetOf(),
                    vcsProcess = false,
                    files = persistentListOf(),
                ),
            )
        }
        verify {
            mockDiffInfoInteractor.unselect()
        }
    }

    // endregion diff selection

    private fun TestScope.sut(init: Sut.() -> Unit = { }): SelectionCoordinator = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private fun sut(init: Sut.() -> Unit = { }): SelectionCoordinator = Sut().apply(init).build()

    private inner class Sut {
        var commitsTableState: CommitsTableState = CommitsTableState.Loading
        var commitInfoState: CommitInfoState = CommitInfoState.None
        var uncommittedChangesState: UncommittedChangesState = UncommittedChangesState.None
        var context: CoroutineContext? = null

        fun build(): SelectionCoordinator {
            every { mockCommitsTableInteractor.state } returns MutableStateFlow(commitsTableState)
            every { mockCommitInfoInteractor.state } returns MutableStateFlow(commitInfoState)
            every { mockUncommittedChangesInteractor.state } returns MutableStateFlow(uncommittedChangesState)

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
