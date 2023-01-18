package ru.ivk1800.vcs.git.command

import ru.ivk1800.vcs.api.command.DiscardCommand
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

internal class DiscardCommandImpl(
    private val directory: Path,
    options: Options,
) : DiscardCommand(options) {
    override suspend fun run() {
        val filePath = Path.of(directory.toString(), options.fileName)
        check(filePath.exists()) { "Unable discard changes, file is not exists: ${options.fileName}" }
        filePath.writeText(options.content)
    }
}
