package ru.ivk1800.diff.feature.repositoryview.presentation

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import ru.ivk1800.diff.feature.repositoryview.domain.FileRepository
import ru.ivk1800.diff.feature.repositoryview.domain.IndexRepository
import java.io.File
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class IndexInteractorTest {

    @RelaxedMockK
    lateinit var mockFileRepository: FileRepository

    @RelaxedMockK
    lateinit var mockIndexRepository: IndexRepository

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    private fun TestScope.sut(init: Sut.() -> Unit = { }): IndexInteractor = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private inner class Sut {
        var context: CoroutineContext? = null

        fun build(): IndexInteractor {
            return IndexInteractor(
                repoDirectory = File(""),
                fileRepository = mockFileRepository,
                indexRepository = mockIndexRepository,
            )
        }
    }
}
