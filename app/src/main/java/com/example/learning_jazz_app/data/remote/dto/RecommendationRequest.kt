package com.example.learning_jazz_app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecommendationRequest(
    @Json(name = "pattern_features") val patternFeatures: Map<String, Double>,
    @Json(name = "limit") val limit: Int = 5
) 