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
import ru.ivk1800.diff.feature.repositoryview.domain.ChangesRepository
import ru.ivk1800.diff.feature.repositoryview.domain.Diff
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.domain.FileRepository
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class ChangesInteractorTest {

    @RelaxedMockK
    lateinit var mockFileRepository: FileRepository

    @RelaxedMockK
    lateinit var mockDiffRepository: DiffRepository

    @RelaxedMockK
    lateinit var mockChangesRepository: ChangesRepository

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
            mockChangesRepository.updateIndex(
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

    @Test
    fun `should discard single hunk with one removed line`() = runTest {
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
        }.discard(
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
            mockChangesRepository.discard(
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

    private fun TestScope.sut(init: Sut.() -> Unit = { }): ChangesInteractor = Sut()
        .apply(init)
        .apply { context = testScheduler }
        .build()

    private inner class Sut {
        var context: CoroutineContext? = null
        var fileLines = emptyList<String>()
        var fileDiff: Diff? = null

        fun build(): ChangesInteractor {
            coEvery { mockChangesRepository.updateIndex(any(), any()) } returns Unit
            coEvery { mockFileRepository.getFileLines(any()) } returns fileLines
            coEvery { mockDiffRepository.getStagedFileDiff(any()) } answers {
                fileDiff ?: error("not implemented")
            }

            return ChangesInteractor(
                fileRepository = mockFileRepository,
                diffRepository = mockDiffRepository,
                changesRepository = mockChangesRepository,
            )
        }
    }
}
