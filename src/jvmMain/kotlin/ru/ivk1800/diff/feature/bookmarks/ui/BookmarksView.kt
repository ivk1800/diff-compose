package ru.ivk1800.diff.feature.bookmarks.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun BookmarksView() {
    Scaffold(
        topBar = {
            TopAppBar { }
        }
    ) {
        LazyColumn {
            items(1000) { id ->
                BookmarkItemView()
                Divider(color = Color.Gray)
            }
        }
    }
}