package ru.ivk1800.vcs.git.command

import ru.ivk1800.vcs.api.command.DiscardCommand
import java.nio.file.Path

internal class DiscardCommandImpl(
    private val directory: Path,
    options: Options,
) : DiscardCommand(options) {
    override suspend fun run() {
        TODO("Not yet implemented")
    }
}
