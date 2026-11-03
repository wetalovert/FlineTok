package com.flinetok.app

import android.content.Context

/** Локальное хранилище: доступ, тема, лайки, избранное, громкость и скрытие просмотренных. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("flinetok", Context.MODE_PRIVATE)

    var askedPermission: Boolean
        get() = sp.getBoolean("asked", false)
        set(v) = sp.edit().putBoolean("asked", v).apply()

    var themeMode: String
        get() = sp.getString("theme", "system") ?: "system"
        set(v) = sp.edit().putString("theme", v).apply()

    var likes: Set<String>
        get() = sp.getStringSet("likes", emptySet())?.toSet() ?: emptySet()
        set(v) = sp.edit().putStringSet("likes", v).apply()

    var favorites: Set<String>
        get() = sp.getStringSet("favorites", emptySet())?.toSet() ?: emptySet()
        set(v) = sp.edit().putStringSet("favorites", v).apply()

    /** quiet, mid, loud или custom */
    var volumeMode: String
        get() = sp.getString("volume_mode", "mid") ?: "mid"
        set(v) = sp.edit().putString("volume_mode", v).apply()

    /** Громкость в процентах для режима custom */
    var volumeCustom: Int
        get() = sp.getInt("volume_custom", 50)
        set(v) = sp.edit().putInt("volume_custom", v).apply()

    var hideSeen: Boolean
        get() = sp.getBoolean("hide_seen", false)
        set(v) = sp.edit().putBoolean("hide_seen", v).apply()

    fun volumeLevel(): Float = when (volumeMode) {
        "quiet" -> 0.2f
        "mid" -> 0.5f
        "loud" -> 1f
        else -> volumeCustom / 100f
    }
}
