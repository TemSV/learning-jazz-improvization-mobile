package com.example.learning_jazz_app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
 
@JsonClass(generateAdapter = true)
data class OriginalChordRef(
    @Json(name = "barid") val barid: Int
) 