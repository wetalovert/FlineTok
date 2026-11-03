package com.flinetok.app

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    prefs: Prefs,
    count: Int,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onAddNew: () -> Unit,
    themeMode: String,
    onThemeChange: (String) -> Unit,
    volumeMode: String,
    volumeCustom: Int,
    onVolumeChange: (mode: String, custom: Int?) -> Unit,
    hideSeen: Boolean,
    onHideSeenChange: (Boolean) -> Unit,
    onOpenVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableIntStateOf(0) }
    Column(modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.tab_general)) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.tab_likes)) })
            Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(R.string.tab_favorites)) })
        }
        when (tab) {
            0 -> GeneralTab(
                hasPermission = hasPermission,
                onRequestPermission = onRequestPermission,
                onAddNew = onAddNew,
                count = count,
                themeMode = themeMode,
                onThemeChange = onThemeChange,
                volumeMode = volumeMode,
                volumeCustom = volumeCustom,
                onVolumeChange = onVolumeChange,
                hideSeen = hideSeen,
                onHideSeenChange = onHideSeenChange
            )
            1 -> VideoList(prefs.likes.toList(), stringResource(R.string.empty_likes), onOpenVideo)
            else -> VideoList(prefs.favorites.toList(), stringResource(R.string.empty_favorites), onOpenVideo)
        }
    }
}

@Composable
private fun GeneralTab(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onAddNew: () -> Unit,
    count: Int,
    themeMode: String,
    onThemeChange: (String) -> Unit,
    volumeMode: String,
    volumeCustom: Int,
    onVolumeChange: (mode: String, custom: Int?) -> Unit,
    hideSeen: Boolean,
    onHideSeenChange: (Boolean) -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (hasPermission) "Доступ выдан. Найдено видео: $count"
            else "Доступ к медиафайлам не выдан"
        )
        if (!hasPermission) {
            Button(onClick = onRequestPermission) { Text(stringResource(R.string.grant_access)) }
        } else {
            Button(onClick = onAddNew) { Text(stringResource(R.string.add_new)) }
        }

        Text(stringResource(R.string.volume), style = MaterialTheme.typography.titleMedium)
        listOf(
            "quiet" to R.string.volume_quiet,
            "mid" to R.string.volume_mid,
            "loud" to R.string.volume_loud,
            "custom" to R.string.volume_custom
        ).forEach { (mode, label) ->
            Row(
                Modifier.fillMaxWidth().clickable { onVolumeChange(mode, null) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = volumeMode == mode, onClick = { onVolumeChange(mode, null) })
                Text(stringResource(label))
            }
        }
        if (volumeMode == "custom") {
            Text("$volumeCustom%")
            Slider(
                value = volumeCustom / 100f,
                onValueChange = { onVolumeChange("custom", (it * 100).toInt()) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.hide_seen))
            Switch(checked = hideSeen, onCheckedChange = onHideSeenChange)
        }

        Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
        listOf(
            "light" to R.string.theme_light,
            "dark" to R.string.theme_dark,
            "system" to R.string.theme_system
        ).forEach { (mode, label) ->
            Row(
                Modifier.fillMaxWidth().clickable { onThemeChange(mode) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = themeMode == mode, onClick = { onThemeChange(mode) })
                Text(stringResource(label))
            }
        }
    }
}

@Composable
private fun VideoList(uris: List<String>, emptyText: String, onOpen: (String) -> Unit) {
    if (uris.isEmpty()) {
        Text(emptyText, modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(uris) { uri ->
            Text(
                text = Uri.parse(uri).lastPathSegment ?: uri,
                modifier = Modifier.fillMaxWidth().clickable { onOpen(uri) }.padding(16.dp)
            )
        }
    }
}
