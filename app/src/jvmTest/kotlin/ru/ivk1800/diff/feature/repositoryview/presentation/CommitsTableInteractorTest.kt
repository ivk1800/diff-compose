package ru.ivk1800.diff.feature.repositoryview.presentation

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Before
import org.junit.Test

class CommitsTableInteractorTest {

    @RelaxedMockK
    lateinit var mockCommitsInteractor: CommitsInteractor

    @RelaxedMockK
    lateinit var mockUncommittedChangesInteractor: UncommittedChangesInteractor

    @RelaxedMockK
    lateinit var mockDiffInfoInteractor: DiffInfoInteractor

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should unselect file of diff if new files selected`() {
        sut().selectCommits(persistentSetOf())
        verify { mockDiffInfoInteractor.unselect() }
    }

    @Test
    fun `should unselect file of diff if uncommitted changes selected`() {
        sut().selectUncommittedChanges()
        verify { mockDiffInfoInteractor.unselect() }
    }

    private fun sut(): CommitsTableInteractor = Sut().build()

    private inner class Sut {
        fun build() = CommitsTableInteractor(
            commitsInteractor = mockCommitsInteractor,
            uncommittedChangesInteractor = mockUncommittedChangesInteractor,
            diffInfoInteractor = mockDiffInfoInteractor,
        )
    }
}
