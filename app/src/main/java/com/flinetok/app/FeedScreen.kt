package com.flinetok.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    videos: List<VideoItem>,
    prefs: Prefs,
    volume: Float,
    hideSeen: Boolean,
    shuffleKey: Int,
    jumpToUri: String?,
    onJumpHandled: () -> Unit,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var likes by remember { mutableStateOf(prefs.likes) }
    var favorites by remember { mutableStateOf(prefs.favorites) }

    // Скрытие уже лайкнутых и избранных роликов.
    val list = remember(videos, hideSeen, likes, favorites) {
        if (hideSeen) videos.filter { val k = it.uri.toString(); k !in likes && k !in favorites }
        else videos
    }

    if (list.isEmpty()) {
        Column(
            modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                when {
                    !hasPermission -> stringResource(R.string.permission_needed)
                    videos.isNotEmpty() -> stringResource(R.string.all_hidden)
                    else -> stringResource(R.string.no_videos)
                }
            )
            if (!hasPermission) {
                IconButton(onClick = onRequestPermission) { Text(stringResource(R.string.grant_access)) }
            }
        }
        return
    }

    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply { repeatMode = Player.REPEAT_MODE_ONE }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, player) {
        // Звук останавливается, когда приложение уходит в фон или закрывается.
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            player.release()
        }
    }

    val pager = rememberPagerState(pageCount = { list.size })
    var paused by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var heartKey by remember { mutableIntStateOf(0) }
    var playKey by remember { mutableIntStateOf(0) }
    val heartScale = remember { Animatable(0.4f) }
    val heartAlpha = remember { Animatable(0f) }
    val playAlpha = remember { Animatable(0f) }

    LaunchedEffect(volume) { player.volume = volume }

    // Зелёное сердце: появляется при лайке двойным тапом и плавно исчезает
    LaunchedEffect(heartKey) {
        if (heartKey > 0) {
            heartAlpha.snapTo(1f)
            heartScale.snapTo(0.4f)
            heartScale.animateTo(1.1f, tween(200))
            heartScale.animateTo(1f, tween(150))
            delay(350)
            heartAlpha.animateTo(0f, tween(250))
        }
    }

    // Значок воспроизведения на короткое время после продолжения
    LaunchedEffect(playKey) {
        if (playKey > 0) {
            playAlpha.snapTo(1f)
            delay(500)
            playAlpha.animateTo(0f, tween(300))
        }
    }

    LaunchedEffect(shuffleKey) { pager.scrollToPage(0) }

    LaunchedEffect(jumpToUri, list) {
        val target = jumpToUri ?: return@LaunchedEffect
        val index = list.indexOfFirst { it.uri.toString() == target }
        if (index >= 0) pager.scrollToPage(index)
        onJumpHandled()
    }

    LaunchedEffect(pager.currentPage, list) {
        val item = list.getOrNull(pager.currentPage) ?: return@LaunchedEffect
        player.setMediaItem(MediaItem.fromUri(item.uri))
        player.prepare()
        player.playWhenReady = !paused
    }

    LaunchedEffect(Unit) {
        while (true) {
            val d = player.duration
            progress = if (d > 0) player.currentPosition.toFloat() / d else 0f
            delay(50)
        }
    }

    Box(modifier.fillMaxSize()) {
        VerticalPager(state = pager, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 1) { page ->
            val item = list[page]
            val key = item.uri.toString()
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(key) {
                        detectTapGestures(
                            onTap = {
                                paused = !paused
                                player.playWhenReady = !paused
                                if (!paused) playKey++
                            },
                            onDoubleTap = {
                                val liking = key !in likes
                                likes = if (liking) likes + key else likes - key
                                prefs.likes = likes
                                if (liking) heartKey++
                            }
                        )
                    }
            ) {
                if (page == pager.currentPage) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                useController = false
                                this.player = player
                            }
                        }
                    )
                }

                Text(
                    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(item.dateMillis)),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 56.dp)
                )

                Column(
                    Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = {
                        likes = if (key in likes) likes - key else likes + key
                        prefs.likes = likes
                    }) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = if (key in likes) MaterialTheme.colorScheme.primary else Color.White
                        )
                    }
                    IconButton(onClick = {
                        favorites = if (key in favorites) favorites - key else favorites + key
                        prefs.favorites = favorites
                    }) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (key in favorites) Color(0xFFF2C46D) else Color.White
                        )
                    }
                }
            }
        }

        // Сердце над роликом
        Icon(
            Icons.Filled.Favorite,
            contentDescription = null,
            tint = Color(0xFF2E9E5B),
            modifier = Modifier
                .align(Alignment.Center)
                .size(120.dp)
                .graphicsLayer {
                    scaleX = heartScale.value
                    scaleY = heartScale.value
                    alpha = heartAlpha.value
                }
        )

        // Значок паузы: висит, пока видео на паузе
        CenterIcon(
            icon = PauseIcon,
            alpha = if (paused) 1f else 0f,
            modifier = Modifier.align(Alignment.Center)
        )

        // Значок воспроизведения: мелькает при продолжении
        CenterIcon(
            icon = PlayIcon,
            alpha = playAlpha.value,
            modifier = Modifier.align(Alignment.Center)
        )

        Slider(
            value = progress,
            onValueChange = {
                progress = it
                val d = player.duration
                if (d > 0) player.seekTo((it * d).toLong())
            },
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun CenterIcon(icon: ImageVector, alpha: Float, modifier: Modifier) {
    Box(
        modifier
            .size(88.dp)
            .graphicsLayer { this.alpha = alpha }
            .background(Color(0x59000000), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
    }
}
