package com.group1.notamonotako.api.requests_responses.notification

import com.google.gson.annotations.SerializedName

data class GetNotification(
    @SerializedName("notes_id")
    val notesId: Int,
    @SerializedName("notes_title")
    val notesTitle: String,
    @SerializedName("notification_type")
    val notificationType: String,
    val email: String,
    val message: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("updated_at")
    val updatedAt: String, // You might want to use a date type
    @SerializedName("created_at")
    val createdAt: String, // You might want to use a date type
    val id: Int
)

data class PostPendingNotification(
    @SerializedName("notes_id")
    val notesId: Int,
    val email: String,
    val message: String
)


