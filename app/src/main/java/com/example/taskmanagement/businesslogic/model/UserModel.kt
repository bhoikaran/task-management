package com.example.taskmanagement.businesslogic.model

data class UserModel(

    val uid: String = "", // keep this if you want to access it in app
    val name: String = "",
    val role: String = "general",
    val email: String = "",
    val createdBy: String = "",
    val sessionId: String = ""
)
