package com.group1.notamonotako.api.requests_responses.notes

import com.google.gson.annotations.SerializedName

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

