package ru.ivk1800.vcs.api.command

interface AddFilesToStagedCommand: Command<Unit> {
    data class Options(val filePaths: List<String>)
}
