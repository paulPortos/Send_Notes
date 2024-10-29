package com.group1.notamonotako.api.requests_responses.signup

data class RegisterRequests(
    val username: String,
    val email: String,
    val password: String
)
data class RegistrationResponses(
    val message: String,  // or another structure based on your API's response
    val token: String?    // if the API provides a token after registration
)
