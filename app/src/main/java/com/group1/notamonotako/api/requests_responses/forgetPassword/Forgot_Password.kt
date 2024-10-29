package com.group1.notamonotako.api.requests_responses.forgetPassword

data class forgot_Password(
    val email: String
)

data class forgot_PasswordResponse(
    val message: String,
    val OTP: String
)


data class reset_Password(
    val email: String,
    val token: String,
    val password: String
)

data class ResetPasswordResponse(
    val message: String
)
