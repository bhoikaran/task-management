package com.example.taskmanagement.viewmodel

import android.app.Application
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.database.AppDatabase
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.model.User
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    val allUsers: LiveData<List<User>>
    val allStatus: List<Status> =  Status.entries

    val filteredTasks = MutableLiveData<List<Task>>()
    var observerNoRecords: ObservableInt = ObservableInt(0)
    var editMode = ObservableBoolean(false)
    init {
        val database = AppDatabase.getInstance(application)
        val taskDao = database.taskDao()
        val userDao = database.userDao()
        repository = TaskRepository(taskDao, userDao)

        allUsers = repository.allUsers.asLiveData()

    }

    fun applyFilters(userId: Int?, status: String?) {
        viewModelScope.launch {
            repository.getFilteredTasks(userId, status)
                .collect { tasks ->
                    filteredTasks.postValue(tasks)
                    observerNoRecords.set(if (tasks.isEmpty()) 2 else 1)
                }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            repository.addTask(task)
        }
    }

    fun loadAllTasks() {

        viewModelScope.launch {
            repository.getAllTask()
                .collect { tasks ->
                    filteredTasks.postValue(tasks)
                    observerNoRecords.set(if (tasks.isEmpty()) 2 else 1)
                }

        }
    }
    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    fun getTaskById(taskId: Int): LiveData<Task?> {
        return repository.getTaskById(taskId)
    }
}