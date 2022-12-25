package ru.ivk1800.diff.compose

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import ru.ivk1800.diff.application.ApplicationTheme

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
    addedTextColor: Color,
    removedColor: Color,
    removedTextColor: Color,
) {
    var addedColor by mutableStateOf(addedColor)
        internal set

    var addedTextColor by mutableStateOf(addedTextColor)
        internal set

    var removedColor by mutableStateOf(removedColor)
        internal set

    var removedTextColor by mutableStateOf(removedTextColor)
        internal set

    fun copy(
        addedColor: Color = this.addedColor,
        addedTextColor: Color = this.addedTextColor,
        removedColor: Color = this.removedColor,
        removedTextColor: Color = this.removedTextColor,
    ) = DiffLinesThemeData(
        addedColor,
        addedTextColor,
        removedColor,
        removedTextColor,
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
        addedColor = Color(0xFF42e25a).copy(alpha = 0.5F),
        addedTextColor = Color(0xFF5cac1b),
        removedColor = Color(0xFFe8876f).copy(alpha = 0.5F),
        removedTextColor = Color(0xFFe8876f),
    )

fun darkDiffLinesTheme() =
    DiffLinesThemeData(
        addedColor = Color(0xFF42e25a).copy(alpha = 0.5F),
        addedTextColor = Color(0xFF42e25a),
        removedColor = Color(0xFFe8716b).copy(alpha = 0.5F),
        removedTextColor = Color(0xFFe8876f),
    )

val LocalDiffTheme = staticCompositionLocalOf<DiffThemeData> { throw IllegalStateException("DiffTheme ot provided.") }

@Composable
fun DiffTheme(
    theme: ApplicationTheme,
    content: @Composable () -> Unit,
) {
    val diffTheme = remember(theme) {
        when (theme) {
            ApplicationTheme.Light -> DiffThemeData(
                commitFileTheme = lightCommitFileTheme(),
                diffLinesTheme = lightDiffLinesTheme(),
            )

            ApplicationTheme.Dark -> DiffThemeData(
                commitFileTheme = darkCommitFileTheme(),
                diffLinesTheme = darkDiffLinesTheme(),
            )
        }
    }
    CompositionLocalProvider(LocalDiffTheme provides diffTheme) {
        MaterialTheme(
            colors = when (theme) {
                ApplicationTheme.Light -> lightColors()
                ApplicationTheme.Dark -> darkColors()
            },
            content = content,
        )
    }
}