package com.example.learning_jazz_app.data.remote.dto

import com.example.learning_jazz_app.domain.model.Song
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SongInfo(
    @Json(name = "id")
    val id: Int,
    @Json(name = "title")
    val title: String?,
    @Json(name = "composer")
    val composer: String?,
    @Json(name = "song_key")
    val key: String?
)

fun SongInfo.toDomain(): Song {
    return Song(
        id = id,
        title = title ?: "Unknown Title",
        composer = composer ?: "Unknown Composer",
        key = key ?: "N/A"
    )
} 