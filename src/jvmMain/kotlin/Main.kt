import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import ru.ivk1800.di.ApplicationScope
import ru.ivk1800.di.compose.LocalApplicationScope
import ru.ivk1800.diff.compose.DiffTheme
import ru.ivk1800.diff.feature.repositoryview.ui.RepositoryViewWindow
import ru.ivk1800.presentation.window.WindowsManager

fun main() = runApplication()

private fun runApplication() = application {
    val applicationScope = remember { ApplicationScope() }

    CompositionLocalProvider(LocalApplicationScope provides applicationScope) {
        runDiffApplication()
    }
}

@Composable
private fun runDiffApplication() {
    DiffTheme {
        val applicationScope = LocalApplicationScope.current
        val windowsManager = applicationScope.windowsManager
        val windows = windowsManager.state.collectAsState()

        windows.value.forEach { window ->
            when (window) {
                WindowsManager.Window.Bookmarks -> applicationScope.bookmarksWindowFactory.create()
                is WindowsManager.Window.Repository -> RepositoryViewWindow(
                    path = window.path,
                    onCloseRequest = { windowsManager.closeRepositoryWindow(window.path) }
                )
            }
        }
    }
}
