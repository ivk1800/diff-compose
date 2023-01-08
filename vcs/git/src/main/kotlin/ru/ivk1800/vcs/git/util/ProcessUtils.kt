package ru.ivk1800.vcs.git.util

import java.nio.file.Path

fun createProcess(directory: Path, command: String): Process =
    ProcessBuilder(*command.split(" ").toTypedArray())
        .directory(directory.toFile())
        .start()

inline fun <T> handleResult(
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
