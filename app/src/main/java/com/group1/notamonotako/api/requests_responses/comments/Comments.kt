package com.group1.notamonotako.api.requests_responses.comments

data class getComments(
    val username: String,
    val comment: String,
    val id: Int,
    val notes_id: Int,
    val user_id: Int

)

data class CommentPostRequest(
    val notes_id: Int,
    val username: String,
    val comment : String,

)

data class CommentPostResponse(
    val user_id: Int,
    val username: String,
    val comment: String,
    val id :Int

)

data class getCommentCount(
    val comment_count: Int
)


