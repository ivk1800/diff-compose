package ru.ivk1800.diff.feature.repositoryview.presentation.manager

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.mapper.CommitInfoMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitId
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommitInfoState
import ru.ivk1800.diff.presentation.ErrorTransformer
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CommitInfoManagerTest {
    @RelaxedMockK
    lateinit var mockCommitsRepository: CommitsRepository

    @RelaxedMockK
    lateinit var mockCommitInfoMapper: CommitInfoMapper

    @RelaxedMockK
    lateinit var mockErrorTransformer: ErrorTransformer

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should reset selected files if new commit is selected`() = runTest {
        val sut = sut()
        sut.selectCommit(CommitId("1"))
        sut.selectFiles(
            persistentSetOf(
                CommitFileId("test"),
            ),
        )
        sut.selectCommit(CommitId("2"))

        sut.state.test {
            skipItems(1)
            assertTrue(awaitItem().asContent().selected.isEmpty())
            expectNoEvents()
        }
    }

    private fun CommitInfoState.asContent(): CommitInfoState.Content = this as CommitInfoState.Content

    private fun sut(): CommitInfoManager = Sut().build()

    private inner class Sut {
        fun build() = CommitInfoManager(
            commitsRepository = mockCommitsRepository,
            commitInfoMapper = mockCommitInfoMapper,
            errorTransformer = mockErrorTransformer,
        )
    }
}
