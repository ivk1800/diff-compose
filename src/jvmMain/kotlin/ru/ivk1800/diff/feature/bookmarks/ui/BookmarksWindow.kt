package ru.ivk1800.diff.feature.bookmarks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import ru.ivk1800.arch.presentation.viewModel
import ru.ivk1800.diff.feature.bookmarks.data.BookmarksStorage
import ru.ivk1800.diff.feature.bookmarks.domain.BookmarksRepository
import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarksInteractor
import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarksRouter
import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarksViewModel
import ru.ivk1800.vcs.git.GitVcs
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

@Composable
fun BookmarksWindow() {
    Window(
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 480.dp, height = 640.dp)
        ),
        onCloseRequest = {},
    ) {
        val viewModel = viewModel {
            BookmarksViewModel(
                router = object : BookmarksRouter {
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
                },
                vcs = GitVcs(),
                bookmarksInteractor = BookmarksInteractor(
                    bookmarksRepository = BookmarksRepository(
                        storage = BookmarksStorage(),
                    ),
                )
            )
        }
        val state by viewModel.state.collectAsState()
        BookmarksView(
            state,
            onEvent = viewModel::onEvent,
        )
    }
}