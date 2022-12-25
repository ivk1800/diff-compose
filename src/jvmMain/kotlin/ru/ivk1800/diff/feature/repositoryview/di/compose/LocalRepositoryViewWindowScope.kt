package ru.ivk1800.diff.feature.repositoryview.di.compose

import androidx.compose.runtime.staticCompositionLocalOf
import ru.ivk1800.diff.feature.repositoryview.di.RepositoryViewWindowScope

val LocalRepositoryViewWindowScope = staticCompositionLocalOf<RepositoryViewWindowScope> {
    throw IllegalStateException("RepositoryViewWindowScope not provided")
}
