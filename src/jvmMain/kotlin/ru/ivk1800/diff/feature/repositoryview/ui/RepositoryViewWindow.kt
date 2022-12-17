package ru.ivk1800.diff.feature.repositoryview.ui

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
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitsInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewViewModel
import ru.ivk1800.vcs.git.GitVcs
import java.io.File

@Composable
fun RepositoryViewWindow(path: String, onCloseRequest: () -> Unit) {
    Window(
        title = path,
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 1280.dp, height = 720.dp)
        ),
        onCloseRequest = onCloseRequest,
    ) {

        val viewModel = viewModel {
            RepositoryViewViewModel(
                commitsInteractor = CommitsInteractor(
                    repoDirectory = File(path),
                    commitsRepository = CommitsRepository(
                        vcs = GitVcs(),
                    ),
                    commitItemMapper = CommitItemMapper()
                )
            )
        }
        val commits by viewModel.commits.collectAsState()
        RepositoryView(commits)
    }
}