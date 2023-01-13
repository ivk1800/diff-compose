package ru.ivk1800.diff.feature.repositoryview.presentation

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import ru.ivk1800.diff.feature.repositoryview.domain.ChangeType
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.model.DiffInfoItem
import ru.ivk1800.diff.presentation.ErrorTransformer
import java.io.File
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class DiffInfoInteractorTest {

    @RelaxedMockK
    lateinit var mockDiffRepository: DiffRepository

    @RelaxedMockK
    lateinit var mockCommitsRepository: CommitsRepository

    @RelaxedMockK
    lateinit var mockDiffInfoItemMapper: DiffInfoItemMapper

    @RelaxedMockK
    lateinit var mockErrorTransformer: ErrorTransformer

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should refresh staged diff`() = runTest {
        val sut = sut {
            stagedFileDiff = Diff(
                filePath = "",
                oldId = "",
                newId = "",
                hunks = persistentListOf(),
                changeType = ChangeType.Modify,
            )
        }
        sut.state.test {
            sut.selectUncommittedFiles(
                fileName = "",
                type = DiffInfoInteractor.UncommittedChangesType.Staged,
            )
            awaitItem()
            sut.refresh()
            awaitItem()
        }
        coVerify(exactly = 2) { mockDiffRepository.getStagedFileDiff(any(), any()) }
    }

    @Test
    fun `should refresh unstaged diff`() = runTest {
        val sut = sut {
            stagedFileDiff = Diff(
                filePath = "",
                oldId = "",
                newId = "",
                hunks = persistentListOf(),
                changeType = ChangeType.Modify,
            )
        }
        sut.state.test {
            sut.selectUncommittedFiles(
                fileName = "",
                type = DiffInfoInteractor.UncommittedChangesType.Staged,
            )
            awaitItem()
            sut.refresh()
            awaitItem()
        }
        coVerify(exactly = 2) { mockDiffRepository.getStagedFileDiff(any(), any()) }
    }

    private fun TestScope.sut(init: Sut.() -> Unit = { }): DiffInfoInteractor = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private inner class Sut {
        var context: CoroutineContext? = null
        var unstagedFileDiff: Diff? = null
        var stagedFileDiff: Diff? = null
        var mappedLines: ImmutableList<DiffInfoItem> = persistentListOf()

        fun build(): DiffInfoInteractor {
            coEvery { mockDiffRepository.getUnstagedFileDiff(any(), any()) } answers {
                unstagedFileDiff ?: error("not implemented")
            }
            coEvery { mockDiffRepository.getStagedFileDiff(any(), any()) } answers {
                stagedFileDiff ?: error("not implemented")
            }
            every { mockDiffInfoItemMapper.mapToItems(any(), any(), any()) } returns mappedLines

            return DiffInfoInteractor(
                diffRepository = mockDiffRepository,
                commitsRepository = mockCommitsRepository,
                diffInfoItemMapper = mockDiffInfoItemMapper,
                errorTransformer = mockErrorTransformer,
                repoDirectory = File(""),
                context = requireNotNull(context)
            )
        }
    }
}