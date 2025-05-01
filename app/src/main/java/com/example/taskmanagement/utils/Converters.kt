package com.example.taskmanagement.utils

import androidx.room.TypeConverter
import com.example.taskmanagement.model.Status
import java.util.Date

// Converters.kt
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromStatus(value: String): Status = enumValueOf(value)

    @TypeConverter
    fun statusToString(status: Status): String = status.name
}