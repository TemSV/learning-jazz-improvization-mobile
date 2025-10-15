package com.example.learning_jazz_app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SongChordsResponse(
    @Json(name = "song_id") val songId: Int,
    @Json(name = "title") val title: String?,
    @Json(name = "chords") val chords: List<SongChordEntry>
) 