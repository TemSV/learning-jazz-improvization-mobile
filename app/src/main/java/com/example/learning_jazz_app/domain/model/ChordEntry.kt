package com.example.learning_jazz_app.domain.model

data class ChordEntry(
    val bar: Int,
    val chords: String,
    val signature: String?,
    val form: String?
) 