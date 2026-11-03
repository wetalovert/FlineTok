package com.flinetok.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun BottomBar(selected: Int, onHome: () -> Unit, onRefresh: () -> Unit, onSettings: () -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == 0,
            onClick = onHome,
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_home)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = onRefresh,
            icon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_refresh)) }
        )
        NavigationBarItem(
            selected = selected == 1,
            onClick = onSettings,
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_settings)) }
        )
    }
}
