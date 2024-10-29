package com.group1.notamonotako.api.requests_responses.public_notes

import com.google.gson.annotations.SerializedName

data class getPublicNotes(
    @SerializedName("id")
    val notesId: Int,
    val title: String,
    val contents: String,
    val public: Boolean,
    val updated_at : String,

)