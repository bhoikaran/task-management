package com.example.taskmanagement.model

import com.google.firebase.firestore.DocumentId

data class TaskModel(
    @DocumentId
    val id: String? = "",
    val title: String = "",
    val taskDetail: String = "",
    val assignPersonId: String = "",
    val assignDate: Long = 0L,
    val completionDate: Long? = null,
    val remark: String? = null,
    val status: String = "IN_PROGRESS",
    val createdBy: String = ""
)
