package com.group1.notamonotako.api.requests_responses.reactions

data class showReactions(
    val id: Int,
    val likes: Int,
    val dislikes: Int
)

data class showSpecificNotes(
    val has_liked: Boolean,
    val has_disliked: Boolean
)