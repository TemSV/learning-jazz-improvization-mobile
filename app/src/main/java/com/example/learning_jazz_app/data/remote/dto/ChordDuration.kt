package com.example.learning_jazz_app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
 
@JsonClass(generateAdapter = true)
data class ChordDuration(
    @Json(name = "chord") val chord: String,
    @Json(name = "duration") val duration: Double
) 