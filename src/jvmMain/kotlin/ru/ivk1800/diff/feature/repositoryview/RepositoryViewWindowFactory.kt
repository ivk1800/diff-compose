package ru.ivk1800.diff.feature.repositoryview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import ru.ivk1800.diff.feature.repositoryview.di.RepositoryViewWindowScope
import ru.ivk1800.diff.feature.repositoryview.di.compose.LocalRepositoryViewWindowScope
import ru.ivk1800.diff.feature.repositoryview.ui.RepositoryViewWindow

class RepositoryViewWindowFactory(
    private val dependencies: RepositoryViewDependencies,
) {
    @Composable
    fun create(id: RepositoryId) {
        val scope = remember(id) { RepositoryViewWindowScope(dependencies, id) }

        CompositionLocalProvider(LocalRepositoryViewWindowScope provides scope) {
            RepositoryViewWindow()
        }
    }
}
