import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import ru.ivk1800.diff.feature.bookmarks.ui.BookmarksWindow
import ru.ivk1800.diff.feature.repositoryview.ui.RepositoryViewWindow
import ru.ivk1800.presentation.window.LocalWindowsManager
import ru.ivk1800.presentation.window.WindowsManager

fun main() = application {
    MaterialTheme(
        colors = darkColors(),
    ) {
        val windowsManager by remember { mutableStateOf(WindowsManager()) }

        CompositionLocalProvider(LocalWindowsManager provides windowsManager) {
            val windows = windowsManager.state.collectAsState()

            windows.value.forEach { window ->
                when (window) {
                    WindowsManager.Window.Bookmarks -> BookmarksWindow()
                    is WindowsManager.Window.Repository -> RepositoryViewWindow(
                        path = window.path,
                        onCloseRequest = { windowsManager.closeRepositoryWindow(window.path) }
                    )
                }
            }
        }
    }
}
