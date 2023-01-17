package ru.ivk1800.diff.feature.repositoryview.presentation.manager

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Before
import org.junit.Test

class CommitsTableManagerTest {

    @RelaxedMockK
    lateinit var mockCommitsManager: CommitsManager

    @RelaxedMockK
    lateinit var mockUncommittedChangesManager: UncommittedChangesManager

    @RelaxedMockK
    lateinit var mockDiffInfoManager: DiffInfoManager

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should unselect file of diff if new files selected`() {
        sut().selectCommits(persistentSetOf())
        verify { mockDiffInfoManager.unselect() }
    }

    @Test
    fun `should unselect file of diff if uncommitted changes selected`() {
        sut().selectUncommittedChanges()
        verify { mockDiffInfoManager.unselect() }
    }

    private fun sut(): CommitsTableManager = Sut().build()

    private inner class Sut {
        fun build() = CommitsTableManager(
            commitsManager = mockCommitsManager,
            uncommittedChangesManager = mockUncommittedChangesManager,
            diffInfoManager = mockDiffInfoManager,
        )
    }
}
