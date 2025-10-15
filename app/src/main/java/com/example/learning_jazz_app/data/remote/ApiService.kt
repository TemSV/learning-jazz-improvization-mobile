package com.example.learning_jazz_app.data.remote

import com.example.learning_jazz_app.data.remote.dto.SongChordsResponse
import com.example.learning_jazz_app.data.remote.dto.SongListResponse
import com.example.learning_jazz_app.data.remote.dto.SongPatternsResponse
import com.example.learning_jazz_app.data.remote.dto.RecommendationRequest
import com.example.learning_jazz_app.data.remote.dto.RecommendationResponse
import com.example.learning_jazz_app.data.remote.dto.NoteDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.Body

interface ApiService {
    @GET("api/songs")
    suspend fun getSongs(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("search") search: String? = null
    ): SongListResponse

    @GET("api/songs/{song_id}/chords")
    suspend fun getSongChords(@Path("song_id") songId: Int): SongChordsResponse

    @GET("api/songs/{song_id}/patterns")
    suspend fun getSongPatterns(@Path("song_id") songId: Int): SongPatternsResponse

    @POST("api/recommendations/phrases")
    suspend fun getPhraseRecommendations(@Body request: RecommendationRequest): RecommendationResponse

    @GET("api/phrases/{melid}/notes")
    suspend fun getPhraseNotes(
        @Path("melid") melid: Int,
        @Query("start_note_index") startNoteId: Int,
        @Query("end_note_index") endNoteId: Int
    ): List<NoteDto>
} 