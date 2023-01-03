package ru.ivk1800.vcs.git

internal enum class GitLogOption(val value: String) {
    Hash("H"),
    AbbreviatedHash("h"),
    RawBody("B"),
    Parents("P"),
    AuthorName("an"),
    AuthorEmail("ae"),
    AuthorDate("at"),
    CommiterName("cn"),
    CommiterEmail("ce"),
    CommiterDate("ct"),
    RefName("D"),
}
