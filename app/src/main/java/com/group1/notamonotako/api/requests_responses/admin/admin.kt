package com.group1.notamonotako.api.requests_responses.admin

import com.google.gson.annotations.SerializedName

class PostToAdmin(
    @SerializedName("notes_id")
    val notesId: Int,
)

class ResponseToAdmin(
    val id: Int,
    val title: String,
    val contents: String,
    val public: Boolean,
    val updated_at : String,
)

data class updateAdminChangesForm(
    val title: String,
    val contents: String,
    val creator_username: String,
    val creator_email: String,
)
