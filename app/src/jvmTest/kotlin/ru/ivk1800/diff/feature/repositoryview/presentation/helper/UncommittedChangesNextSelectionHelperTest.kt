package ru.ivk1800.diff.feature.repositoryview.presentation.helper

import kotlinx.collections.immutable.persistentSetOf
import org.junit.Test
import ru.ivk1800.diff.feature.repositoryview.presentation.model.CommitFileId
import kotlin.test.assertEquals

class UncommittedChangesNextSelectionHelperTest {
    @Test
    fun `should calculate index for first file`() {
        val result = sut().calculateIndex(
            allFiles = listOf(
                "1",
                "2",
                "3",
            ),
            id = CommitFileId("1")
        )
        assertEquals(0, result)
    }

    @Test
    fun `should calculate index for last file`() {
        val result = sut().calculateIndex(
            allFiles = listOf(
                "1",
                "2",
                "3",
            ),
            id = CommitFileId("3")
        )
        assertEquals(2, result)
    }

    @Test
    fun `should calculate index for center file`() {
        val result = sut().calculateIndex(
            allFiles = listOf(
                "1",
                "2",
                "3",
            ),
            id = CommitFileId("2")
        )
        assertEquals(1, result)
    }

    @Test
    fun `should return file if first removed`() {
        val result = sut().confirm(
            allFiles = listOf(
                "2",
                "3",
            ),
            removedFileIndex = 0,
        )
        assertEquals(
            persistentSetOf(
                CommitFileId("2"),
            ),
            result,
        )
    }

    @Test
    fun `should return file if center removed`() {
        val result = sut().confirm(
            allFiles = listOf(
                "1",
                "3",
            ),
            removedFileIndex = 1,
        )
        assertEquals(
            persistentSetOf(
                CommitFileId("3"),
            ),
            result,
        )
    }

    @Test
    fun `should return file if last removed`() {
        val result = sut().confirm(
            allFiles = listOf(
                "1",
                "2",
            ),
            removedFileIndex = 2,
        )
        assertEquals(
            persistentSetOf(
                CommitFileId("2"),
            ),
            result,
        )
    }

    @Test
    fun `should return file if all removed`() {
        val result = sut().confirm(
            allFiles = emptyList(),
            removedFileIndex = 0,
        )
        assertEquals(
            persistentSetOf(),
            result,
        )
    }

    @Test
    fun `should return file if index out bounds`() {
        val result = sut().confirm(
            allFiles = listOf(
                "1",
                "2",
            ),
            removedFileIndex = 10,
        )
        assertEquals(
            persistentSetOf(
                CommitFileId("2"),
            ),
            result,
        )
    }

    private fun sut(init: Sut.() -> Unit = { }): UncommittedChangesNextSelectionHelper = Sut().apply(init).build()

    private inner class Sut {
        fun build(): UncommittedChangesNextSelectionHelper = UncommittedChangesNextSelectionHelper()
    }
}
