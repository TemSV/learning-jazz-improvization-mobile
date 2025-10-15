package com.example.learning_jazz_app.data.remote.dto

import com.example.learning_jazz_app.domain.model.Note
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NoteDto(
    @Json(name = "pitch") val pitch: Int,
    @Json(name = "onset") val onset: Double,
    @Json(name = "duration") val duration: Double,
    @Json(name = "loud_med") val volume: Double,
    @Json(name = "f0_med_dev") val pitchDeviation: Double
)

fun NoteDto.toDomain(): Note {
    return Note(
        pitch = pitch,
        onset = onset,
        duration = duration,
        volume = volume
    )
} 