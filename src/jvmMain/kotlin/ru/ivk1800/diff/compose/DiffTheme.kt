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
data class DiffThemeData(
    val commitFileTheme: CommitFileThemeData,
    val diffLinesTheme: DiffLinesThemeData,
)

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

@Stable
class DiffLinesThemeData(
    addedColor: Color,
    removedColor: Color,
) {
    var addedColor by mutableStateOf(addedColor)
        internal set

    var removedColor by mutableStateOf(removedColor)
        internal set

    fun copy(
        addedColor: Color = this.addedColor,
        removedColor: Color = this.removedColor,
    ) = DiffLinesThemeData(
        addedColor,
        removedColor,
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

fun lightDiffLinesTheme() =
    DiffLinesThemeData(
        addedColor = Color(0xFF033B42),
        removedColor = Color(0xFF502127),
    )

fun darkDiffLinesTheme() =
    DiffLinesThemeData(
        addedColor = Color(0xFF033B42),
        removedColor = Color(0xFF502127),
    )

val LocalDiffTheme = staticCompositionLocalOf<DiffThemeData> { throw IllegalStateException("DiffTheme ot provided.") }

@Composable
fun DiffTheme(
    content: @Composable () -> Unit,
) {
    val diffTheme = DiffThemeData(
        commitFileTheme = darkCommitFileTheme(),
        diffLinesTheme = darkDiffLinesTheme(),
    )
    CompositionLocalProvider(LocalDiffTheme provides diffTheme) {
        MaterialTheme(
            colors = darkColors(),
            content = content,
        )
    }
}