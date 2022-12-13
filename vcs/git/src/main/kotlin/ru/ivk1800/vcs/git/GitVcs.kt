package ru.ivk1800.vcs.git

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.ivk1800.vcs.api.Vcs
import java.io.File

class GitVcs : Vcs {
    override suspend fun isRepository(directory: File): Boolean =
        withContext(Dispatchers.IO) {
            if (!directory.exists()) {
                false
            } else {
                createProcess(directory, "git rev-parse --git-dir").exitValue() == 0
            }
        }

    private fun createProcess(directory: File, command: String): Process =
        ProcessBuilder(*command.split(" ").toTypedArray())
            .directory(directory).start()
}
