package com.example.taskmanagement.businesslogic.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId


data class TitleModel(
    @DocumentId
    val id: String? = "",
    val name: String = "",
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
)