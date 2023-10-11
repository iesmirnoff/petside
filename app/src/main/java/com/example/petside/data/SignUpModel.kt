package com.example.petside.data

data class SignUpResponseModel(
    val message: String? = "",
)

data class SignUpRequestModel(
    val email: String,
    val appDescription: String
)