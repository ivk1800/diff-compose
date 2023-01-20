package ru.ivk1800.diff.feature.repositoryview.presentation.manager

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import ru.ivk1800.diff.feature.repositoryview.domain.ChangeType
import ru.ivk1800.diff.feature.repositoryview.domain.CommitFile
import ru.ivk1800.diff.feature.repositoryview.domain.Status
import ru.ivk1800.diff.feature.repositoryview.domain.StatusRepository
import ru.ivk1800.diff.feature.repositoryview.domain.UncommittedRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.helper.UncommittedChangesNextSelectionHelper
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.state.UncommittedChangesState
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UncommittedChangesManagerTest {

    @RelaxedMockK
    lateinit var mockStatusRepository: StatusRepository

    @RelaxedMockK
    lateinit var mockUncommittedRepository: UncommittedRepository

    @RelaxedMockK
    lateinit var mockUncommittedChangesNextSelectionHelper: UncommittedChangesNextSelectionHelper

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should emit staged selected files`() = runTest {
        val selected = persistentSetOf<CommitFileId>(mockk())
        val sut = sut {
            staged = listOf(TestFile)
        }

        sut.state.test {
            sut.check()
            sut.selectStatedFiles(selected)

            skipItems(1)
            assertEquals(selected, awaitItem().asContent().staged.selected)
            expectNoEvents()
        }
    }

    @Test
    fun `should emit empty unstaged selected files if select staged`() = runTest {
        val selected = persistentSetOf<CommitFileId>(mockk())
        val sut = sut {
            staged = listOf(TestFile)
        }

        sut.state.test {
            sut.check()
            sut.selectStatedFiles(selected)

            skipItems(1)
            assertTrue(awaitItem().asContent().unstaged.selected.isEmpty())
            expectNoEvents()
        }
    }

    @Test
    fun `should emit unstaged selected files`() = runTest {
        val selected = persistentSetOf<CommitFileId>(mockk())
        val sut = sut {
            staged = listOf(TestFile)
        }

        sut.state.test {
            sut.check()
            sut.selectUnstatedFiles(selected)

            skipItems(1)
            assertEquals(selected, awaitItem().asContent().unstaged.selected)
            expectNoEvents()
        }
    }

    @Test
    fun `should emit empty staged selected files if select unstaged`() = runTest {
        val selected = persistentSetOf<CommitFileId>(mockk())
        val sut = sut {
            staged = listOf(TestFile)
        }

        sut.state.test {
            sut.check()
            sut.selectUnstatedFiles(selected)

            skipItems(1)
            assertTrue(awaitItem().asContent().staged.selected.isEmpty())
            expectNoEvents()
        }
    }

    @Test
    fun `should switch selected files`() = runTest {
        val selected = persistentSetOf<CommitFileId>(mockk())
        val sut = sut {
            staged = listOf(TestFile)
        }
        sut.state.test {
            sut.check()
            sut.selectStatedFiles(selected)
            sut.selectUnstatedFiles(selected)

            skipItems(1)
            val content = awaitItem().asContent()
            assertTrue(content.staged.selected.isEmpty())
            assertTrue(content.unstaged.selected.isNotEmpty())
            expectNoEvents()
        }
    }

    @Test
    fun `should unselect staged files`() = runTest {
        val sut = sut {
            staged = listOf(TestFile)
        }
        sut.state.test {
            sut.check()
            sut.selectStatedFiles(persistentSetOf<CommitFileId>(mockk()))
            sut.selectStatedFiles(persistentSetOf())

            skipItems(1)
            val content = awaitItem().asContent()
            assertTrue(content.staged.selected.isEmpty())
            expectNoEvents()
        }
    }

    @Test
    fun `should unselect unstaged files`() = runTest {
        val sut = sut {
            staged = listOf(TestFile)
        }
        sut.state.test {
            sut.check()
            sut.selectUnstatedFiles(persistentSetOf<CommitFileId>(mockk()))
            sut.selectUnstatedFiles(persistentSetOf())

            skipItems(1)
            val content = awaitItem().asContent()
            assertTrue(content.staged.selected.isEmpty())
            expectNoEvents()
        }
    }

    @Test
    fun `should select next file after add to staged`() = runTest {
        val sut = sut {
            staged = listOf(TestFile)
            nextSelectedUnstagedFile = CommitFileId("2")
        }
        sut.state.test {
            sut.addFilesToStaged(
                persistentSetOf<CommitFileId>(
                    CommitFileId("1"),
                )
            )

            skipItems(2)
            val selected = awaitItem().asContent().unstaged.selected
            assertEquals(1, selected.size)
            assertEquals(CommitFileId("2"), selected.first())
            expectNoEvents()
        }
    }

    @Test
    fun `should select next file after remove from staged`() = runTest {
        val sut = sut {
            staged = listOf(TestFile)
            nextSelectedUnstagedFile = CommitFileId("2")
        }
        sut.state.test {
            sut.removeFilesFromStaged(
                persistentSetOf<CommitFileId>(
                    CommitFileId("1"),
                )
            )

            skipItems(2)
            val selected = awaitItem().asContent().staged.selected
            assertEquals(1, selected.size)
            assertEquals(CommitFileId("2"), selected.first())
            expectNoEvents()
        }
    }

    @Test
    fun `should reset selection if remove multiple files from staged`() = runTest {
        val sut = sut {
            staged = listOf(TestFile)
        }
        sut.state.test {
            sut.selectStatedFiles(persistentSetOf<CommitFileId>(mockk(), mockk()))
            sut.removeFilesFromStaged(
                persistentSetOf<CommitFileId>(
                    CommitFileId("1"),
                    CommitFileId("2"),
                )
            )

            skipItems(2)
            val selected = awaitItem().asContent().staged.selected
            assertTrue(selected.isEmpty())
            expectNoEvents()
        }
    }

    @Test
    fun `should reset selection if add multiple files to staged`() = runTest {
        val sut = sut {
            staged = listOf(TestFile)
        }
        sut.state.test {
            sut.selectUnstatedFiles(persistentSetOf<CommitFileId>(mockk(), mockk()))
            sut.addFilesToStaged(
                persistentSetOf<CommitFileId>(
                    CommitFileId("1"),
                    CommitFileId("2"),
                )
            )

            skipItems(2)
            val selected = awaitItem().asContent().unstaged.selected
            assertTrue(selected.isEmpty())
            expectNoEvents()
        }
    }

    @Test
    fun `should reset selection if add all files to staged`() = runTest {
        val sut = sut {
            staged = listOf(TestFile)
        }
        sut.state.test {
            sut.selectUnstatedFiles(persistentSetOf<CommitFileId>(mockk()))
            sut.addAllToStaged()

            skipItems(2)
            val selected = awaitItem().asContent().unstaged.selected
            assertTrue(selected.isEmpty())
            expectNoEvents()
        }
    }

    @Test
    fun `should reset selection if add all files to unstaged`() = runTest {
        val sut = sut {
            staged = listOf(TestFile)
        }
        sut.state.test {
            sut.selectStatedFiles(persistentSetOf<CommitFileId>(mockk()))
            sut.removeAllFromStaged()

            skipItems(2)
            val selected = awaitItem().asContent().staged.selected
            assertTrue(selected.isEmpty())
            expectNoEvents()
        }
    }

    private fun UncommittedChangesState.asContent(): UncommittedChangesState.Content =
        this as UncommittedChangesState.Content

    private fun TestScope.sut(init: Sut.() -> Unit = { }): UncommittedChangesManager = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private inner class Sut {
        var staged: List<CommitFile> = emptyList()
        var unstaged: List<CommitFile> = emptyList()
        var untracked: List<CommitFile> = emptyList()
        var nextSelectedUnstagedFile: CommitFileId? = null
        var context: CoroutineContext? = null

        fun build(): UncommittedChangesManager {
            coEvery { mockStatusRepository.getStatus() } returns Status(
                branch = "",
                staged = staged,
                unstaged = unstaged,
                untracked = untracked,
            )
            coEvery { mockUncommittedChangesNextSelectionHelper.confirm(any(), any()) }.returns(
                nextSelectedUnstagedFile?.let {
                    persistentSetOf(it)
                } ?: persistentSetOf()
            )

            return UncommittedChangesManager(
                statusRepository = mockStatusRepository,
                uncommittedRepository = mockUncommittedRepository,
                selectionHelper = mockUncommittedChangesNextSelectionHelper,
                context = requireNotNull(context),
            )
        }
    }

    private companion object {
        private val TestFile = CommitFile(
            name = "1",
            changeType = ChangeType.Modify,
        )
    }
}
