package com.example.learning_jazz_app.domain.repository

import com.example.learning_jazz_app.data.remote.dto.SongChordsResponse
import com.example.learning_jazz_app.data.remote.dto.SongPatternsResponse
import com.example.learning_jazz_app.data.remote.dto.RecommendationRequest
import com.example.learning_jazz_app.data.remote.dto.RecommendationResponse
import com.example.learning_jazz_app.domain.model.Note
import com.example.learning_jazz_app.domain.model.Song

interface SongRepository {
    suspend fun getSongs(): Result<List<Song>>
    suspend fun getSongChords(songId: Int): Result<SongChordsResponse>
    suspend fun getSongPatterns(songId: Int): Result<SongPatternsResponse>
    suspend fun getPhraseRecommendations(request: RecommendationRequest): Result<RecommendationResponse>
    suspend fun getPhraseNotes(melid: Int, startNoteId: Int, endNoteId: Int): Result<List<Note>>
} 