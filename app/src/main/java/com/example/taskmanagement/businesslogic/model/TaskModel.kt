package com.example.taskmanagement.businesslogic.model

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
    val userRemark: String? = null,
    val status: Status = Status.IN_PROGRESS,
    val createdBy: String = ""
)
