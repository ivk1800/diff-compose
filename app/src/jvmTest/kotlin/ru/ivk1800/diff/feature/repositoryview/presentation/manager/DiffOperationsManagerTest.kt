package ru.ivk1800.diff.feature.repositoryview.presentation.manager

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import org.junit.Before
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class DiffOperationsManagerTest {

    @RelaxedMockK
    lateinit var mockFilesInfoManager: FilesInfoManager

    @RelaxedMockK
    lateinit var mockDiffInfoManager: DiffInfoManager

    @RelaxedMockK
    lateinit var mockChangesManager: ChangesManager

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    private fun TestScope.sut(init: Sut.() -> Unit = { }): DiffOperationsManager = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private inner class Sut {
        var context: CoroutineContext? = null

        fun build(): DiffOperationsManager {
            return DiffOperationsManager(
                filesInfoManager = mockFilesInfoManager,
                diffInfoManager = mockDiffInfoManager,
                changesManager = mockChangesManager,
                context = requireNotNull(context),
            )
        }
    }
}
