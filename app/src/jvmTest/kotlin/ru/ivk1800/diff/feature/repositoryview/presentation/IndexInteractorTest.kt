package ru.ivk1800.diff.feature.repositoryview.presentation

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import ru.ivk1800.diff.feature.repositoryview.domain.ChangeType
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.domain.FileRepository
import ru.ivk1800.diff.feature.repositoryview.domain.IndexRepository
import java.io.File
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class IndexInteractorTest {

    @RelaxedMockK
    lateinit var mockFileRepository: FileRepository

    @RelaxedMockK
    lateinit var mockDiffRepository: DiffRepository

    @RelaxedMockK
    lateinit var mockIndexRepository: IndexRepository

    @Before
    fun before() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should remove single hunk from stage with one removed line`() = runTest {
        sut {
            fileLines = listOf(
                "A",
                "B",
                "C",
                "D",
                "",
            )
            fileDiff = Diff(
                filePath = "",
                oldId = "",
                newId = "",
                hunks = listOf(
                    Diff.Hunk(
                        firstRange = IntRange(1, 4),
                        secondRange = IntRange(1, 3),
                        lines = listOf(
                            Diff.Hunk.Line(
                                text = "A",
                                type = Diff.Hunk.Line.Type.NotChanged,
                            ),
                            Diff.Hunk.Line(
                                text = "B",
                                type = Diff.Hunk.Line.Type.Removed,
                            ),
                            Diff.Hunk.Line(
                                text = "C",
                                type = Diff.Hunk.Line.Type.NotChanged,
                            ),
                            Diff.Hunk.Line(
                                text = "D",
                                type = Diff.Hunk.Line.Type.NotChanged,
                            ),
                        ),
                    )
                ),
                changeType = ChangeType.Modify,
            )
        }.removeFromIndex(
            fileName = "Test.txt",
            hunk = Diff.Hunk(
                firstRange = IntRange(1, 4),
                secondRange = IntRange(1, 3),
                lines = listOf(
                    Diff.Hunk.Line(
                        text = "A",
                        type = Diff.Hunk.Line.Type.NotChanged,
                    ),
                    Diff.Hunk.Line(
                        text = "B",
                        type = Diff.Hunk.Line.Type.Removed,
                    ),
                    Diff.Hunk.Line(
                        text = "C",
                        type = Diff.Hunk.Line.Type.NotChanged,
                    ),
                    Diff.Hunk.Line(
                        text = "D",
                        type = Diff.Hunk.Line.Type.NotChanged,
                    ),
                ),
            ),
        )

        coVerify {
            mockIndexRepository.updateIndex(
                any(),
                any(),
                content = """
                    A
                    B
                    C
                    D
                    
            """.trimIndent(),
            )
        }
    }

    private fun TestScope.sut(init: Sut.() -> Unit = { }): IndexInteractor = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private inner class Sut {
        var context: CoroutineContext? = null
        var fileLines = emptyList<String>()
        var fileDiff: Diff? = null

        fun build(): IndexInteractor {
            coEvery { mockIndexRepository.updateIndex(any(), any(), any()) } returns Unit
            coEvery { mockFileRepository.getFileLines(any(), any()) } returns fileLines
            coEvery { mockDiffRepository.getStagedFileDiff(any(), any()) } answers {
                fileDiff ?: error("not implemented")
            }

            return IndexInteractor(
                repoDirectory = File(""),
                fileRepository = mockFileRepository,
                diffRepository = mockDiffRepository,
                indexRepository = mockIndexRepository,
            )
        }
    }
}
