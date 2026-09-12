package com.example

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestPTR() {
    var isRefreshing by remember { mutableStateOf(false) }
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { isRefreshing = true }
    ) {
        Box(Modifier)
    }
}
