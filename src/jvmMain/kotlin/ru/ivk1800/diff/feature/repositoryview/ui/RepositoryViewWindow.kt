package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import ru.ivk1800.diff.feature.repositoryview.domain.CommitsRepository
import ru.ivk1800.diff.feature.repositoryview.domain.DiffRepository
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitInfoMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.CommitsInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoInteractor
import ru.ivk1800.diff.feature.repositoryview.presentation.DiffInfoItemMapper
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewEvent
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewRouter
import ru.ivk1800.diff.feature.repositoryview.presentation.RepositoryViewViewModel
import ru.ivk1800.diff.presentation.viewModel
import ru.ivk1800.vcs.git.GitVcs
import java.io.File
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RepositoryViewWindow(path: String, onCloseRequest: () -> Unit) {
    val viewModel = viewModel {
        RepositoryViewViewModel(
            repositoryDirectory = File(path),
            commitsInteractor = CommitsInteractor(
                repoDirectory = File(path),
                commitsRepository = CommitsRepository(
                    vcs = GitVcs(),
                ),
                commitItemMapper = CommitItemMapper(),
            ),
            commitInfoInteractor = CommitInfoInteractor(
                repoDirectory = File(path),
                commitsRepository = CommitsRepository(
                    vcs = GitVcs(),
                ),
                commitInfoMapper = CommitInfoMapper(),
            ),
            diffInfoInteractor = DiffInfoInteractor(
                repoDirectory = File(path),
                diffRepository = DiffRepository(
                    vcs = GitVcs(),
                ),
                commitsRepository = CommitsRepository(
                    vcs = GitVcs(),
                ),
                diffInfoItemMapper = DiffInfoItemMapper(),
            ),
            router = object : RepositoryViewRouter {
                override fun toTerminal(directory: File) {
                    ProcessBuilder(*"open -a Terminal ${directory.path}".split(" ").toTypedArray())
                        .start()
                        .apply { waitFor(10, TimeUnit.SECONDS) }
                }

                override fun toFinder(directory: File) {
                    ProcessBuilder(*"open -a Finder ${directory.path}".split(" ").toTypedArray())
                        .start()
                        .apply { waitFor(10, TimeUnit.SECONDS) }
                }
            },
        )
    }

    Window(
        title = path,
        state = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = 1280.dp, height = 720.dp)
        ),
        onCloseRequest = onCloseRequest,
        onKeyEvent = { event ->
            if (event.isMetaPressed && event.key == Key.R) {
                viewModel.onEvent(RepositoryViewEvent.OnReload)
                true
            } else {
                false
            }
        }
    ) {

        val state by viewModel.state.collectAsState()
        RepositoryView(state, onEvent = viewModel::onEvent)
    }
}
