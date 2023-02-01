package ru.ivk1800.diff.feature.repositoryview.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ivk1800.diff.feature.repositoryview.presentation.state.CommandsActivityState


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CommandsActivityView(
    state: CommandsActivityState,
) =
    Column(
        modifier = Modifier.height(16.dp)
    ) {
        val activeCommand = state.activeCommand
        Divider()
        AnimatedContent(
            modifier = Modifier.wrapContentWidth().align(Alignment.End),
            targetState = activeCommand,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedVisibility(visible = it != null) {
                    CircularProgressIndicator(modifier = Modifier.size(12.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = it.orEmpty(),
                    style = MaterialTheme.typography.caption,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
