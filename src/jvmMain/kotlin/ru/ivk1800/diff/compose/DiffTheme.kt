package ru.ivk1800.diff.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class DiffThemeData(val commitFileTheme: CommitFileThemeData)

@Stable
class CommitFileThemeData(
    modifiedColor: Color,
    addedColor: Color,
    renamedColor: Color,
    deletedColor: Color,
    textColor: Color,
) {
    var modifiedColor by mutableStateOf(modifiedColor)
        internal set

    var addedColor by mutableStateOf(addedColor)
        internal set

    var renamedColor by mutableStateOf(renamedColor)
        internal set

    var deletedColor by mutableStateOf(deletedColor)
        internal set

    var textColor by mutableStateOf(textColor)
        internal set

    fun copy(
        modifiedColor: Color = this.modifiedColor,
        addedColor: Color = this.addedColor,
        renamedColor: Color = this.renamedColor,
        deletedColor: Color = this.deletedColor,
        textColor: Color = this.textColor,
    ) = CommitFileThemeData(
        modifiedColor,
        addedColor,
        renamedColor,
        deletedColor,
        textColor,
    )
}

fun lightCommitFileTheme() =
    CommitFileThemeData(
        modifiedColor = Color(0xFFf1940b),
        addedColor = Color(0xFF4fad08),
        renamedColor = Color(0xFF418df6),
        deletedColor = Color(0xFFeb594e),
        textColor = Color.White,
    )

fun darkCommitFileTheme() =
    CommitFileThemeData(
        modifiedColor = Color(0xFFf1940b),
        addedColor = Color(0xFF4fad08),
        renamedColor = Color(0xFF418df6),
        deletedColor = Color(0xFFeb594e),
        textColor = Color.Black,
    )


val LocalDiffTheme = staticCompositionLocalOf<DiffThemeData> { throw IllegalStateException("DiffTheme ot provided.") }

@Composable
fun DiffTheme(
    content: @Composable () -> Unit,
) {
    val diffTheme = DiffThemeData(
        commitFileTheme = darkCommitFileTheme()
    )
    CompositionLocalProvider(LocalDiffTheme provides diffTheme) {
        MaterialTheme(
            colors = darkColors(),
            content = content,
        )
    }
}