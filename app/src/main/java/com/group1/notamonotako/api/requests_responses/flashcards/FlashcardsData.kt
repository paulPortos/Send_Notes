package com.group1.notamonotako.api.requests_responses.flashcards

import com.google.gson.annotations.SerializedName

data class GetFlashcards (
    val id: Int,
    val title: String ,
    val cards: List<String>,
    @SerializedName("updated_at")
    val updatedAt: String,
)
data class PostFlashcards(
    val title: String,
    val cards: List<String>,
    val public: Boolean,
    val to_public: Boolean
)

data class FlashcardsResponse(
    val id: Int,
    val title: String,
    val cards: List<String>,
    val public: Boolean,
    val to_public: Boolean,
    val updated_at: String
)

data class UpdateFlashcards(
    val title: String,
    val cards: List<String>
)