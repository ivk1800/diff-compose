package ru.ivk1800.vcs.git.command

import java.nio.file.Path

abstract class BaseCommand {

    protected fun createProcess(directory: Path, command: String): Process =
        ProcessBuilder(*command.split(" ").toTypedArray())
            .directory(directory.toFile())
            .start()

    protected inline fun <T> handleResult(
        process: Process,
        onResult: (result: String) -> T,
        onError: (error: String) -> Throwable,
    ): T = if (process.waitFor() != 0) {
        val error = process.errorStream.reader().readText()
        throw onError.invoke(error)
    } else {
        val result = process.inputStream.reader().readText()
        onResult.invoke(result)
    }
}