package ru.ivk1800.vcs.git

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.ivk1800.vcs.api.VcsCommit
import ru.ivk1800.vcs.api.VcsFile
import java.lang.reflect.Type

internal class VcsParser {
    private val commitsListType: Type = object : TypeToken<List<RawCommit>>() {}.type
    private val gson = Gson()

    fun parseCommits(raw: String): List<VcsCommit> {
        val rawCommits: List<RawCommit> =
            gson.fromJson<List<RawCommit?>>(raw, commitsListType).filterIsInstance<RawCommit>()

        return rawCommits.map { it.toVcsCommit() }
    }

    fun parseCommit(raw: String): VcsCommit {
        val rawCommit: RawCommit = gson.fromJson(raw, RawCommit::class.java)
        return rawCommit.toVcsCommit()
    }

    fun parseFiles(raw: String): List<VcsFile> =
        raw
            .lines()
            .drop(1)
            .filter(CharSequence::isNotEmpty)
            .map { VcsFile(fullPath = it) }

    private fun RawCommit.toVcsCommit(): VcsCommit =
        VcsCommit(
            hash = hash,
            parents = parents.split(" "),
            abbreviatedHash = abbreviatedHash,
            authorName = authorName,
            authorEmail = authorEmail,
            authorDate = authorDate.toInt(),
            commiterName = commiterName,
            commiterEmail = commiterEmail,
            commiterDate = commiterDate.toInt(),
            message = message,
        )
}
