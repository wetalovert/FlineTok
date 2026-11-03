package com.flinetok.app

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Океанская палитра, из которой строятся схемы, если динамические цвета недоступны.
private val OceanLight = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF0B7A99),
    onPrimary = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFCDEFF5),
    background = androidx.compose.ui.graphics.Color(0xFFF3F9FA),
    surface = androidx.compose.ui.graphics.Color(0xFFF3F9FA),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFDCEFF4)
)

private val OceanDark = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF7FD3E6),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF05232B),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF1B4A58),
    background = androidx.compose.ui.graphics.Color(0xFF0B1E26),
    surface = androidx.compose.ui.graphics.Color(0xFF0B1E26),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF1B3F4B)
)

@Composable
fun FlineTokTheme(themeMode: String, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val context = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> OceanDark
        else -> OceanLight
    }
    MaterialTheme(colorScheme = scheme, content = content)
}
