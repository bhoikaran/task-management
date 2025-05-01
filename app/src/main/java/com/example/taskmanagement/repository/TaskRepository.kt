package com.example.taskmanagement.repository

import androidx.lifecycle.LiveData
import com.example.taskmanagement.network.UserDao
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.network.TaskDao

class TaskRepository(private val taskDao: TaskDao, private val userDao: UserDao) {
    val allUsers = userDao.getAllUsers()

    fun getFilteredTasks(userId: Int?, status: String?) =
        taskDao.getFilteredTasks(userId, status)

    fun getAllTask() =
        taskDao.getAllTask()

    suspend fun addTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)
    fun getTaskById(taskId: Int): LiveData<Task?> {
        return taskDao.getTaskById(taskId)
    }

}