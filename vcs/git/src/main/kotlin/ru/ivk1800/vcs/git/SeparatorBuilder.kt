package ru.ivk1800.vcs.git

internal class SeparatorBuilder {

    fun startRecordSeparator(): String = StartRecord
    fun endRecordSeparator(): String = EndRecord

    fun buildStartForOption(option: GitLogOption): String = "\u0001${option.value}\u0001"

    fun buildEndForOption(option: GitLogOption): String = "\u0003${option.value}\u0003"

    private companion object {
        private const val StartRecord = "\u0001\u0001"
        private const val EndRecord = "\u0003\u0003"
    }
}
