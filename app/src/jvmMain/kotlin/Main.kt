import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import ru.ivk1800.diff.compose.DiffTheme
import ru.ivk1800.diff.di.ApplicationScope
import ru.ivk1800.diff.di.compose.LocalApplicationScope
import ru.ivk1800.diff.window.WindowsManager

fun main() = runApplication()

private fun runApplication() = application {
    val applicationScope = remember { ApplicationScope() }

    CompositionLocalProvider(LocalApplicationScope provides applicationScope) {
        runDiffApplication()
    }
}

@Composable
private fun runDiffApplication() {
    val applicationScope = LocalApplicationScope.current
    val themeState = applicationScope.applicationThemeProvider.theme.collectAsState()

    DiffTheme(
        theme = themeState.value
    ) {
        val windowsManager = applicationScope.windowsManager
        val windows = windowsManager.state.collectAsState()

        windows.value.forEach { window ->
            when (window) {
                WindowsManager.Window.Bookmarks -> applicationScope.bookmarksWindowFactory.create()
                is WindowsManager.Window.Repository -> applicationScope.repositoryViewWindowFactory.create(window.id)
            }
        }
    }
}
