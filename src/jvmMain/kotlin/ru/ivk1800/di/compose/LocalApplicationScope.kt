package ru.ivk1800.di.compose

import androidx.compose.runtime.staticCompositionLocalOf
import ru.ivk1800.di.ApplicationScope

val LocalApplicationScope = staticCompositionLocalOf<ApplicationScope> {
    throw IllegalStateException("ApplicationScope not provided")
}
