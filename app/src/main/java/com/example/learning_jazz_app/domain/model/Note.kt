package com.example.learning_jazz_app.domain.model

data class Note(
    val pitch: Int, // MIDI pitch
    val onset: Double, // in seconds
    val duration: Double, // in seconds
    val volume: Double // 0.0 to 1.0
) 