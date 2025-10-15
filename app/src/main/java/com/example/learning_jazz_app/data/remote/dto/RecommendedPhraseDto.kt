package com.example.learning_jazz_app.data.remote.dto

import com.example.learning_jazz_app.domain.model.RecommendedPhrase
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecommendedPhraseDto(
    @Json(name = "melid") val melid: Int,
    @Json(name = "start_note_index") val startNoteIndex: Int,
    @Json(name = "end_note_index") val endNoteIndex: Int,
    @Json(name = "similarity") val similarity: Double,
    @Json(name = "chords") val chords: List<ChordDuration>,
    @Json(name = "performer") val performer: String?,
    @Json(name = "title") val title: String?
)

fun RecommendedPhraseDto.toDomain(): RecommendedPhrase {
    return RecommendedPhrase(
        melid = melid,
        performer = performer ?: "Unknown Artist",
        title = title ?: "Unknown Song",
        startNoteIndex = startNoteIndex,
        endNoteIndex = endNoteIndex
    )
} 