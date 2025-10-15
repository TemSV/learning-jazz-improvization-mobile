package com.example.learning_jazz_app.data.repository

import com.example.learning_jazz_app.data.remote.ApiService
import com.example.learning_jazz_app.data.remote.dto.RecommendationRequest
import com.example.learning_jazz_app.data.remote.dto.RecommendationResponse
import com.example.learning_jazz_app.data.remote.dto.SongChordsResponse
import com.example.learning_jazz_app.data.remote.dto.SongPatternsResponse
import com.example.learning_jazz_app.data.remote.dto.toDomain
import com.example.learning_jazz_app.domain.model.Note
import com.example.learning_jazz_app.domain.model.Song
import com.example.learning_jazz_app.domain.repository.SongRepository

class SongRepositoryImpl(
    private val apiService: ApiService
) : SongRepository {
    override suspend fun getSongs(): Result<List<Song>> {
        return try {
            val response = apiService.getSongs()
            val songs = response.songs.map { it.toDomain() }
            Result.success(songs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSongChords(songId: Int): Result<SongChordsResponse> {
        return try {
            Result.success(apiService.getSongChords(songId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSongPatterns(songId: Int): Result<SongPatternsResponse> {
        return try {
            Result.success(apiService.getSongPatterns(songId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPhraseRecommendations(request: RecommendationRequest): Result<RecommendationResponse> {
        return try {
            Result.success(apiService.getPhraseRecommendations(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPhraseNotes(
        melid: Int,
        startNoteId: Int,
        endNoteId: Int
    ): Result<List<Note>> {
        return try {
            val noteDtos = apiService.getPhraseNotes(
                melid = melid,
                startNoteId = startNoteId,
                endNoteId = endNoteId
            )
            Result.success(noteDtos.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 