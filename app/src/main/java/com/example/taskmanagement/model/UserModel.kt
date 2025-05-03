package com.example.taskmanagement.model

data class UserModel(

    val uid: String = "", // keep this if you want to access it in app
    val name: String = "",
    val role: String = "general",
    val email: String = ""
)
