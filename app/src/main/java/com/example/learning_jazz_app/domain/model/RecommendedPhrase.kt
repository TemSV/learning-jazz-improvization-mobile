package com.example.learning_jazz_app.domain.model

data class RecommendedPhrase(
    val melid: Int,
    val performer: String,
    val title: String,
    val startNoteIndex: Int,
    val endNoteIndex: Int
) 