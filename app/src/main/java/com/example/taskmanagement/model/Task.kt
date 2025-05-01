package com.example.taskmanagement.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.taskmanagement.model.User

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["assignPersonId"],
        onDelete = ForeignKey.CASCADE
    )]
)
/*data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val taskDetail: String,
    val assignDate: Long, // Store as timestamp
    val assignPersonId: Int,
    val completionDate: Long?,
    val remark: String?,
    val status: Status
)*/
//@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val taskDetail: String,
    val assignPersonId: Int,
    val assignDate: Long, // store as timestamp (in millis)
    val completionDate: Long?,
    val remark: String?,
    val status: Status = Status.IN_PROGRESS
)