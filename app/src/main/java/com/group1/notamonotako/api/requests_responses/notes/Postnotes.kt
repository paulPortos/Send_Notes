package com.group1.notamonotako.api.requests_responses.notes

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

