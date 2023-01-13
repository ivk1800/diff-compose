package ru.ivk1800.diff.feature.repositoryview.presentation

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class DiffOperationsInteractorTest {

    @RelaxedMockK
    lateinit var mockFilesInfoInteractor: FilesInfoInteractor

    @RelaxedMockK
    lateinit var mockDiffInfoInteractor: DiffInfoInteractor

    @RelaxedMockK
    lateinit var mockIndexInteractor: IndexInteractor

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    private fun TestScope.sut(init: Sut.() -> Unit = { }): DiffOperationsInteractor = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private inner class Sut {
        var context: CoroutineContext? = null

        fun build(): DiffOperationsInteractor {
            return DiffOperationsInteractor(
                filesInfoInteractor = mockFilesInfoInteractor,
                diffInfoInteractor = mockDiffInfoInteractor,
                indexInteractor = mockIndexInteractor,
                context = requireNotNull(context),
            )
        }
    }
}
