package com.group1.notamonotako.api.requests_responses.notes

import com.google.gson.annotations.SerializedName


data class Note(
    val id: Int,
    val title: String,
    val contents: String,
    val updated_at : String,
    val public: Boolean,
    @SerializedName("to_public")
    val toPublic: Boolean
)

data class UpdateNotes(
    val title: String,
    val contents: String,
    @SerializedName("to_public")
    val toPublic: Boolean,
    val public: Boolean
)
data class UpdateToPublicNotes(
    val title: String,
    val contents: String,
    @SerializedName("to_public")
    val toPublic: Boolean
)

data class PostnotesRequest(

    val title: String,
    val contents: String,
    val public: Boolean,
    val to_public: Boolean
)

data class PostnotesResponse(
    val message: String,
    val id: Int,
    val user_id: Int,
    val updated_at: String
)