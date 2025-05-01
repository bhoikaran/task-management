package com.example.taskmanagement.network

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTask(): Flow<List<Task>>


    /*@Query("SELECT * FROM tasks WHERE (:userId IS NULL OR assignPersonId = :userId) AND (:status IS NULL OR status = :status)")
       fun getFilteredTasks(userId: Int?, status: Status?): Flow<List<Task>>*/


    @Query("SELECT * FROM tasks WHERE (:userId IS NULL OR assignPersonId = :userId) AND (:status IS NULL OR status = :status) ORDER BY id DESC")
    fun getFilteredTasks(userId: Int?, status: String?): Flow<List<Task>>

    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    fun getTaskById(taskId: Int): LiveData<Task?>
}