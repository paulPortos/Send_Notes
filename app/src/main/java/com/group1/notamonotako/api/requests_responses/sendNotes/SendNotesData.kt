package com.group1.notamonotako.api.requests_responses.sendNotes

import com.google.gson.annotations.SerializedName


data class getSentNotesData(
    val id: Int,
    val title: String,
    val contents: String,
    @SerializedName("notes_id")
    val notesId: Int,
    @SerializedName("send_to")
    val sendTo: String,
    @SerializedName("sent_by")
    val sentBy: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class SendNotesRequest(
    @SerializedName("notes_id")
    val notesId: Int,
    @SerializedName("send_to")
    val sendTo: String,
    @SerializedName("sent_by")
    val sentBy: String,
)
