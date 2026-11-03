package com.flinetok.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Собственные значки паузы и воспроизведения, чтобы не зависеть от расширенного набора иконок. */
val PauseIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Pause", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .addPath(
            pathData = addPathNodes("M6 5h4v14H6z M14 5h4v14h-4z"),
            fill = SolidColor(Color.White)
        ).build()
}

val PlayIcon: ImageVector by lazy {
    ImageVector.Builder(name = "Play", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .addPath(
            pathData = addPathNodes("M8 5v14l11-7z"),
            fill = SolidColor(Color.White)
        ).build()
}
