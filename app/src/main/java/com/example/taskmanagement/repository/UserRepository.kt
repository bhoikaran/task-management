package com.example.taskmanagement.repository

import com.example.taskmanagement.model.User
import com.example.taskmanagement.network.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun insert(user: User) {
        userDao.insertUser(user)
    }

    suspend fun update(user: User) {
        userDao.updateUser(user)
    }

    suspend fun delete(user: User) {
        userDao.deleteUser(user)
    }

    // Updated without email parameter
    suspend fun updateUserById(id: Int, name: String) {
        userDao.updateUserById(id, name)
    }
}
