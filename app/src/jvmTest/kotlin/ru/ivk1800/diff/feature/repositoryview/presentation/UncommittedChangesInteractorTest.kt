package ru.ivk1800.diff.feature.repositoryview.presentation

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
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UncommittedChangesInteractorTest {

    @RelaxedMockK
    lateinit var mockCommitInfoMapper: CommitInfoMapper

    @RelaxedMockK
    lateinit var mockDiffRepository: DiffRepository

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should emit staged selected files`() = runTest {
        val selected = persistentSetOf<CommitFileId>(mockk())
        val sut = sut {
            stagedDiff = listOf(
                mockk()
            )
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
            stagedDiff = listOf(
                mockk()
            )
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
            stagedDiff = listOf(
                mockk()
            )
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
            stagedDiff = listOf(
                mockk()
            )
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
            stagedDiff = listOf(
                mockk()
            )
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

    private fun UncommittedChangesState.asContent(): UncommittedChangesState.Content =
        this as UncommittedChangesState.Content

    private fun TestScope.sut(init: Sut.() -> Unit = { }): UncommittedChangesInteractor = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private inner class Sut {
        var stagedDiff: List<Diff> = emptyList()
        var unstagedDiff: List<Diff> = emptyList()
        var context: CoroutineContext? = null

        fun build(): UncommittedChangesInteractor {
            coEvery { mockDiffRepository.getStagedDiff(any()) } returns stagedDiff
            coEvery { mockDiffRepository.getUnstagedDiff(any()) } returns unstagedDiff

            return UncommittedChangesInteractor(
                repoDirectory = File(""),
                commitInfoMapper = mockCommitInfoMapper,
                diffRepository = mockDiffRepository,
                context = requireNotNull(context),
            )
        }
    }
}
