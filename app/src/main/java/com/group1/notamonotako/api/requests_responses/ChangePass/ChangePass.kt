package com.group1.notamonotako.api.requests_responses.ChangePass

data class ChangePasswordRequest(
    val old_password: String,
    val password: String
)

data class ChangePasswordResponse(
    val message: String,
)