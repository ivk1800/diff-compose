package ru.ivk1800.diff.feature.bookmarks.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ru.ivk1800.diff.feature.bookmarks.presentation.BookmarksState

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun BookmarksView(state: BookmarksState) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) with fadeOut(animationSpec = tween(90))
        },
    ) { targetState ->
        when (targetState) {
            is BookmarksState.Content ->
                LazyColumn {
                    items(targetState.items.size) { index ->
                        val item = targetState.items[index]
                        BookmarkItemView(item)
                        Divider(color = Color.Gray)
                    }
                }

            BookmarksState.Loading -> Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}