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

        return rawCommits.map {
            VcsCommit(
                hash = it.hash,
                abbreviatedHash = it.abbreviatedHash,
                authorName = it.authorName,
                authorEmail = it.authorEmail,
                authorDate = it.authorDate.toInt(),
                commiterName = it.commiterName,
                commiterEmail = it.commiterEmail,
                commiterDate = it.commiterDate.toInt(),
                message = it.message,
            )
        }
    }

    fun parseFiles(raw: String): List<VcsFile> =
        raw
            .lines()
            .drop(1)
            .filter(CharSequence::isNotEmpty)
            .map { VcsFile(fullPath = it) }
}
