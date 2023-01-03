package ru.ivk1800.vcs.git.parser

import org.junit.Test
import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.git.SeparatorBuilder
import kotlin.test.assertEquals

class GitLogParserTest {

    // region ref names

    @Test
    fun `should parse empty ref names`() {
        val result = sut().parseSingleLog(
            raw {
                refNames = ""
            }
        )

        assertEquals(emptyList(), result.refNames)
    }

    @Test
    fun `should parse one ref name`() {
        val result = sut().parseSingleLog(
            raw {
                refNames = "HEAD -> master"
            }
        )

        assertEquals(1, result.refNames.size)
        assertEquals("HEAD -> master", result.refNames[0])
    }

    @Test
    fun `should parse two ref name`() {
        val result = sut().parseSingleLog(
            raw {
                refNames = "origin/master, origin/HEAD"
            }
        )

        assertEquals(2, result.refNames.size)
        assertEquals("origin/master", result.refNames[0])
        assertEquals("origin/HEAD", result.refNames[1])
    }

    // endregion ref names

    private fun GitLogParser.parseSingleLog(raw: String): VcsCommit {
        return parseLog(raw).first()
    }

    private fun sut(): GitLogParser =
        Sut().build()

    private class Sut {
        fun build() = GitLogParser(
            separatorBuilder = SeparatorBuilder(),
        )
    }

    private fun raw(init: RawBuilder.() -> Unit): String =
        RawBuilder().apply(init).build()

    private class RawBuilder {
        var refNames: String = ""

        fun build(): String {
            return """

H
a542f194ba9421d2886094f0e371833e17bbd543
H
P
7ea4110c4668b1e590ef947c66f9cc8a361e2444
P
h
a542f19
h
B
Add select diff event

B
an
Ivan
an
ae
ivan@ivk1800.ru
ae
at
1672749406
at
cn
Ivan
cn
ce
ivan@ivk1800.ru
ce
ct
1672749406
ct
D
$refNames
D

            """.trimIndent()
        }
    }
}
