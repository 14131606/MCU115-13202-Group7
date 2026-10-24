package com.example.telephonebook

data class Contact(
    val name: String,
    val phone: String,
    val email: String = ""
)