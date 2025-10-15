package com.example.learning_jazz_app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SongListResponse(
    @Json(name = "songs")
    val songs: List<SongInfo>,
    @Json(name = "total")
    val total: Int
) 