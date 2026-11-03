package com.flinetok.app

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

data class VideoItem(val uri: Uri, val dateMillis: Long)

object VideoRepository {

    /**
     * Ищет видео только в папках TikTok и MargyT.
     * Фильтр выполняется в запросе MediaStore, остальные файлы не читаются.
     */
    fun load(context: Context): List<VideoItem> {
        val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.DATE_ADDED
        )
        val selection = "(${MediaStore.Video.Media.RELATIVE_PATH} LIKE ? OR ${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?)"
        val args = arrayOf("%/TikTok/%", "%/MargyT/%")

        val result = mutableListOf<VideoItem>()
        context.contentResolver.query(collection, projection, selection, args, null)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val takenCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)
            val addedCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val taken = c.getLong(takenCol)
                val added = c.getLong(addedCol) * 1000L
                val date = if (taken > 0) taken else added
                result += VideoItem(ContentUris.withAppendedId(collection, id), date)
            }
        }
        return result.shuffled()
    }
}
