package ru.ivk1800.navigation

import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarksRouter
import ru.ivk1800.presentation.window.WindowsManager
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

class BookmarksRouterImpl(
    private val windowsManager: WindowsManager
) : BookmarksRouter {
    override fun toChooseRepositoryFolder(callback: (value: File) -> Unit) {
        val fileChooser = JFileChooser(FileSystemView.getFileSystemView()).apply {
            currentDirectory = File(System.getProperty("user.dir"))
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isAcceptAllFileFilterUsed = true
            selectedFile = null
            currentDirectory = null
        }
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            callback.invoke(file)
        }
    }

    override fun toRepository(path: String) {
        windowsManager.openRepositoryWindowIfAbsent(path)
    }
}
