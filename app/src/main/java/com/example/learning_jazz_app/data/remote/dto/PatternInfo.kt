package com.example.learning_jazz_app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatternInfo(
    @Json(name = "type") val type: String,
    @Json(name = "key") val key: String?,
    @Json(name = "original_chord_refs") val originalChordRefs: List<OriginalChordRef>,
    @Json(name = "chords") val chords: List<ChordDuration>,
    @Json(name = "features") val features: Map<String, Double>
) 