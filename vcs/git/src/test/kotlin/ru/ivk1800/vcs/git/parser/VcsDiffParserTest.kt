package ru.ivk1800.vcs.git.parser

import org.junit.Test
import ru.ivk1800.vcs.api.VcsDiff
import ru.ivk1800.vcs.git.test.RawDiffBuilder
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VcsDiffParserTest {

    // region new file

    @Test
    fun `should parse type of added new file`() {
        val result = sut().parseSingle {
            diff(
                old = "/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt",
                new = "/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt",
            )
            newFile(mode = 100644)
            index(
                old = "0000000",
                new = "1041785",
            )
            oldFileName("/dev/null")
            newFileName("/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt")
            hunkHeader("@@ -0,0 +1,3 @@")
            hunkLine("+package ru.ivk1800.diff")
            hunkLine("+")
            hunkLine("+class Test {}")
        }

        assertTrue(result is VcsDiff.Added)
    }

    @Test
    fun `should parse file name of added new file`() {
        val result = sut().parseSingle {
            diff(
                old = "/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt",
                new = "/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt",
            )
            newFile(mode = 100644)
            index(
                old = "0000000",
                new = "1041785",
            )
            oldFileName("/dev/null")
            newFileName("/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt")
            hunkHeader("@@ -0,0 +1,3 @@")
            hunkLine("+package ru.ivk1800.diff")
            hunkLine("+")
            hunkLine("+class Test {}")
        }

        assertEquals(
            "src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt",
            result.asAdded().fileName,
        )
    }

    @Test
    fun `should parse old id of added new file`() {
        val result = sut().parseSingle {
            diff(
                old = "/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt",
                new = "/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt",
            )
            newFile(mode = 100644)
            index(
                old = "0000000",
                new = "1041785",
            )
            oldFileName("/dev/null")
            newFileName("/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt")
            hunkHeader("@@ -0,0 +1,3 @@")
            hunkLine("+package ru.ivk1800.diff")
            hunkLine("+")
            hunkLine("+class Test {}")
        }

        assertEquals(
            "0000000",
            result.asAdded().oldId,
        )
    }

    @Test
    fun `should parse new id of added new file`() {
        val result = sut().parseSingle {
            diff(
                old = "/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt",
                new = "/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt",
            )
            newFile(mode = 100644)
            index(
                old = "0000000",
                new = "1041785",
            )
            oldFileName("/dev/null")
            newFileName("/src/jvmMain/kotlin/ru/ivk1800/diff/Test.kt")
            hunkHeader("@@ -0,0 +1,3 @@")
            hunkLine("+package ru.ivk1800.diff")
            hunkLine("+")
            hunkLine("+class Test {}")
        }

        assertEquals(
            "1041785",
            result.asAdded().newId,
        )
    }

    // region empty file

    @Test
    fun `should parse type of added new empty file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/Test",
                new = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/Test",
            )
            newFile(mode = 100644)
            index(
                old = "0000000",
                new = "e69de29"
            )
        }

        assertTrue(result is VcsDiff.Added)
    }

    @Test
    fun `should parse empty hunks of added new empty file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/Test",
                new = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/Test",
            )
            newFile(mode = 100644)
            index(
                old = "0000000",
                new = "e69de29"
            )
        }

        assertTrue(result.asAdded().hunks.isEmpty())
    }

    // endregion empty file

    // endregion new file

    // region modified file

    @Test
    fun `should parse diff of modified file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt",
                new = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt",
            )
            index(
                old = "6d8687d",
                new = "4e36e1c",
                mode = 100644,
            )
            oldFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt")
            newFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt")
            hunkHeader(
                header = "@@ -0,0 +1,3 @@",
                line = " import ru.ivk1800.vcs.api.VcsCommit",
            )
            hunkLine(" import ru.ivk1800.vcs.api.VcsDiff")
            hunkLine(" import ru.ivk1800.vcs.api.VcsFile")
            hunkLine(" import ru.ivk1800.vcs.git.parser.GitLogParser")
            hunkLine("+import ru.ivk1800.vcs.git.parser.VcsDiffParser")
            hunkLine(" import java.io.File")
            hunkLine(" import java.util.concurrent.TimeUnit")
            hunkLine(" import kotlin.io.path.Path")
        }

        assertTrue(result is VcsDiff.Modified)
    }

    @Test
    fun `should parse old id of modified file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt",
                new = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt",
            )
            index(
                old = "6d8687d",
                new = "4e36e1c",
                mode = 100644,
            )
            oldFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt")
            newFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt")
            hunkHeader(
                header = "@@ -0,0 +1,3 @@",
                line = " import ru.ivk1800.vcs.api.VcsCommit",
            )
            hunkLine(" import ru.ivk1800.vcs.api.VcsDiff")
            hunkLine(" import ru.ivk1800.vcs.api.VcsFile")
            hunkLine(" import ru.ivk1800.vcs.git.parser.GitLogParser")
            hunkLine("+import ru.ivk1800.vcs.git.parser.VcsDiffParser")
            hunkLine(" import java.io.File")
            hunkLine(" import java.util.concurrent.TimeUnit")
            hunkLine(" import kotlin.io.path.Path")
        }

        assertEquals(
            "6d8687d",
            result.asModified().oldId,
        )
    }

    @Test
    fun `should parse new id of modified file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt",
                new = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt",
            )
            index(
                old = "6d8687d",
                new = "4e36e1c",
                mode = 100644,
            )
            oldFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt")
            newFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt")
            hunkHeader(
                header = "@@ -0,0 +1,3 @@",
                line = " import ru.ivk1800.vcs.api.VcsCommit",
            )
            hunkLine(" import ru.ivk1800.vcs.api.VcsDiff")
            hunkLine(" import ru.ivk1800.vcs.api.VcsFile")
            hunkLine(" import ru.ivk1800.vcs.git.parser.GitLogParser")
            hunkLine("+import ru.ivk1800.vcs.git.parser.VcsDiffParser")
            hunkLine(" import java.io.File")
            hunkLine(" import java.util.concurrent.TimeUnit")
            hunkLine(" import kotlin.io.path.Path")
        }

        assertEquals(
            "4e36e1c",
            result.asModified().newId,
        )
    }

    @Test
    fun `should parse file name of modified file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt",
                new = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt",
            )
            index(
                old = "6d8687d",
                new = "4e36e1c",
                mode = 100644,
            )
            oldFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt")
            newFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt")
            hunkHeader(
                header = "@@ -0,0 +1,3 @@",
                line = " import ru.ivk1800.vcs.api.VcsCommit",
            )
            hunkLine(" import ru.ivk1800.vcs.api.VcsDiff")
            hunkLine(" import ru.ivk1800.vcs.api.VcsFile")
            hunkLine(" import ru.ivk1800.vcs.git.parser.GitLogParser")
            hunkLine("+import ru.ivk1800.vcs.git.parser.VcsDiffParser")
            hunkLine(" import java.io.File")
            hunkLine(" import java.util.concurrent.TimeUnit")
            hunkLine(" import kotlin.io.path.Path")
        }

        assertEquals(
            "vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/GitVcs.kt",
            result.asModified().fileName,
        )
    }

    // endregion modified file

    // region moved file

    @Test
    fun `should parse old file name moved file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt",
                new = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt",
            )
            similarityIndex(98)
            renameFrom("vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt")
            renameTo("vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt")
            index(
                old = "afbef14",
                new = "4b4dc23",
                mode = 100644,
            )
            oldFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt")
            newFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt")
        }

        assertEquals(
            "vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt",
            result.asMoved().renameFrom,
        )
    }

    @Test
    fun `should parse new file name moved file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt",
                new = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt",
            )
            similarityIndex(98)
            renameFrom("vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt")
            renameTo("vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt")
            index(
                old = "afbef14",
                new = "4b4dc23",
                mode = 100644,
            )
            oldFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt")
            newFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt")
        }

        assertEquals(
            "vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt",
            result.asMoved().renameTo,
        )
    }

    @Test
    fun `should parse old id moved file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt",
                new = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt",
            )
            similarityIndex(98)
            renameFrom("vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt")
            renameTo("vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt")
            index(
                old = "afbef14",
                new = "4b4dc23",
                mode = 100644,
            )
            oldFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt")
            newFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt")
        }

        assertEquals(
            "afbef14",
            result.asMoved().oldId,
        )
    }

    @Test
    fun `should parse new id moved file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt",
                new = "/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt",
            )
            similarityIndex(98)
            renameFrom("vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt")
            renameTo("vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt")
            index(
                old = "afbef14",
                new = "4b4dc23",
                mode = 100644,
            )
            oldFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/VcsDiffParser.kt")
            newFileName("/vcs/git/src/main/kotlin/ru/ivk1800/vcs/git/parser/VcsDiffParser.kt")
        }

        assertEquals(
            "4b4dc23",
            result.asMoved().newId,
        )
    }

    // endregion moved file

    // region deleted file

    @Test
    fun `should parse type of deleted file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt",
                new = "/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt",
            )
            deletedFile(100644)
            index(
                old = "40757b1",
                new = "0000000",
            )
            oldFileName("/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt")
            newFileName("/dev/null")
            hunkHeader("-package ru.ivk1800.vcs.git")
            hunkHeader("-")
            hunkHeader("-class VcsDiffParserTest {")
            hunkHeader("    -")
            hunkHeader("    -}")
        }

        assertTrue(result is VcsDiff.Deleted)
    }

    @Test
    fun `should parse old id of deleted file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt",
                new = "/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt",
            )
            deletedFile(100644)
            index(
                old = "40757b1",
                new = "0000000",
            )
            oldFileName("/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt")
            newFileName("/dev/null")
            hunkHeader("-package ru.ivk1800.vcs.git")
            hunkHeader("-")
            hunkHeader("-class VcsDiffParserTest {")
            hunkHeader("    -")
            hunkHeader("    -}")
        }

        assertEquals(
            "40757b1",
            result.asDeleted().oldId,
        )
    }

    @Test
    fun `should parse new id of deleted file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt",
                new = "/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt",
            )
            deletedFile(100644)
            index(
                old = "40757b1",
                new = "0000000",
            )
            oldFileName("/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt")
            newFileName("/dev/null")
            hunkHeader("-package ru.ivk1800.vcs.git")
            hunkHeader("-")
            hunkHeader("-class VcsDiffParserTest {")
            hunkHeader("    -")
            hunkHeader("    -}")
        }

        assertEquals(
            "0000000",
            result.asDeleted().newId,
        )
    }

    @Test
    fun `should parse file name of deleted file`() {
        val result = sut().parseSingle {
            diff(
                old = "/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt",
                new = "/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt",
            )
            deletedFile(100644)
            index(
                old = "40757b1",
                new = "0000000",
            )
            oldFileName("/vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt")
            newFileName("/dev/null")
            hunkHeader("-package ru.ivk1800.vcs.git")
            hunkHeader("-")
            hunkHeader("-class VcsDiffParserTest {")
            hunkHeader("    -")
            hunkHeader("    -}")
        }

        assertEquals(
            "vcs/git/src/test/kotlin/ru/ivk1800/vcs/git/VcsDiffParserTest.kt",
            result.asDeleted().fileName,
        )
    }

    // endregion deleted file

    private fun VcsDiffParser.parseSingle(raw: RawDiffBuilder.() -> Unit): VcsDiff =
        parseSingle(RawDiffBuilder().apply(raw).build())

    private fun sut(): VcsDiffParser =
        Sut().build()

    private fun VcsDiff.asDeleted(): VcsDiff.Deleted = this as VcsDiff.Deleted

    private fun VcsDiff.asMoved(): VcsDiff.Moved = this as VcsDiff.Moved

    private fun VcsDiff.asModified(): VcsDiff.Modified = this as VcsDiff.Modified

    private fun VcsDiff.asAdded(): VcsDiff.Added = this as VcsDiff.Added

    private class Sut {
        fun build() = VcsDiffParser()
    }
}
