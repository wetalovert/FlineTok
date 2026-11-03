package com.flinetok.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { FlineTokApp() }
    }
}

fun mediaPermission(): String =
    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_VIDEO
    else Manifest.permission.READ_EXTERNAL_STORAGE

fun hasMediaPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, mediaPermission()) == PackageManager.PERMISSION_GRANTED

@Composable
fun FlineTokApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { Prefs(context) }

    var themeMode by remember { mutableStateOf(prefs.themeMode) }
    var volumeMode by remember { mutableStateOf(prefs.volumeMode) }
    var volumeCustom by remember { mutableIntStateOf(prefs.volumeCustom) }
    var hideSeen by remember { mutableStateOf(prefs.hideSeen) }
    var granted by remember { mutableStateOf(hasMediaPermission(context)) }
    var feedVideos by remember { mutableStateOf(emptyList<VideoItem>()) }
    var shuffleKey by remember { mutableIntStateOf(0) }
    var tab by remember { mutableIntStateOf(0) }
    var jumpToUri by remember { mutableStateOf<String?>(null) }
    var addedCount by remember { mutableStateOf<Int?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
    }

    LaunchedEffect(Unit) {
        if (!granted && !prefs.askedPermission) {
            prefs.askedPermission = true
            launcher.launch(mediaPermission())
        }
    }

    LaunchedEffect(granted) {
        if (granted) {
            feedVideos = withContext(Dispatchers.IO) { VideoRepository.load(context) }.shuffled()
        }
    }

    LaunchedEffect(addedCount) {
        if (addedCount != null) {
            delay(2500)
            addedCount = null
        }
    }

    /** Кнопка «Обновить» в нижней панели: меняет порядок показа роликов. */
    fun reshuffle() {
        feedVideos = feedVideos.shuffled()
        shuffleKey++
    }

    /** Кнопка «Добавить новые видео» в настройках: ищет новые файлы в галерее. */
    fun addNewVideos() {
        if (!granted) {
            launcher.launch(mediaPermission())
            return
        }
        scope.launch {
            val before = feedVideos.map { it.uri }.toSet()
            val fresh = withContext(Dispatchers.IO) { VideoRepository.load(context) }
            val added = fresh.filter { it.uri !in before }
            feedVideos = feedVideos + added.shuffled()
            addedCount = added.size
        }
    }

    FlineTokTheme(themeMode) {
        Scaffold(
            bottomBar = {
                BottomBar(
                    selected = tab,
                    onHome = { tab = 0 },
                    onRefresh = {
                        tab = 0
                        reshuffle()
                    },
                    onSettings = { tab = 1 }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    0 -> FeedScreen(
                        videos = feedVideos,
                        prefs = prefs,
                        volume = prefs.volumeLevel(),
                        hideSeen = hideSeen,
                        shuffleKey = shuffleKey,
                        jumpToUri = jumpToUri,
                        onJumpHandled = { jumpToUri = null },
                        hasPermission = granted,
                        onRequestPermission = { launcher.launch(mediaPermission()) },
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> SettingsScreen(
                        prefs = prefs,
                        count = feedVideos.size,
                        hasPermission = granted,
                        onRequestPermission = { launcher.launch(mediaPermission()) },
                        onAddNew = { addNewVideos() },
                        themeMode = themeMode,
                        onThemeChange = { themeMode = it; prefs.themeMode = it },
                        volumeMode = volumeMode,
                        volumeCustom = volumeCustom,
                        onVolumeChange = { mode, custom ->
                            volumeMode = mode
                            prefs.volumeMode = mode
                            if (custom != null) {
                                volumeCustom = custom
                                prefs.volumeCustom = custom
                            }
                        },
                        hideSeen = hideSeen,
                        onHideSeenChange = { hideSeen = it; prefs.hideSeen = it },
                        onOpenVideo = { uri ->
                            jumpToUri = uri
                            tab = 0
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                addedCount?.let { n ->
                    Text(
                        text = "Добавлено: $n",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
