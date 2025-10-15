package com.example.learning_jazz_app.domain.model

data class SongChords(
    val songId: Int,
    val title: String,
    val composer: String,
    val chords: List<ChordEntry>
) 