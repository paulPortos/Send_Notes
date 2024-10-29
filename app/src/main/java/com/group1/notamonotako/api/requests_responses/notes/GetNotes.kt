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

) {
}

data class NoteRequest(
    val title: String,
    val contents: String,
    val public: Boolean,
    val to_public: Boolean
)