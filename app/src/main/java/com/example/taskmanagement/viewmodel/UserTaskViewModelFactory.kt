package com.example.taskmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanagement.repository.FirestoreTaskRepository



class UserTaskViewModelFactory(
    private val repo: FirestoreTaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserNewTaskViewModel(repo) as T
    }
}
