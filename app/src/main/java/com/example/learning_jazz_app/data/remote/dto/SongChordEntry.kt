package com.example.learning_jazz_app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SongChordEntry(
    @Json(name = "barid") val barid: Int,
    @Json(name = "bar") val bar: Int,
    @Json(name = "signature") val signature: String?,
    @Json(name = "chords") val chords: String?,
    @Json(name = "form") val form: String?
) 