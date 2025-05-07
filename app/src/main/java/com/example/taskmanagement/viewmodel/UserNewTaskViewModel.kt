package com.example.taskmanagement.viewmodel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.TaskModel
import com.example.taskmanagement.model.UserModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await





class UserNewTaskViewModel(
    private val repo: FirestoreTaskRepository
) : ViewModel() {



    // Filtered tasks
    private val _filteredTasks = MutableLiveData<List<TaskModel>>()
    val filteredTasks: LiveData<List<TaskModel>> = _filteredTasks

    // UI state
    var observerNoRecords = ObservableInt(0)
    var editMode = ObservableBoolean(false)


    /** Create or update a task */
    fun addTask(task: TaskModel) = viewModelScope.launch(Dispatchers.IO) {
        repo.addOrUpdateTask(task)
    }

    fun updateTask(task: TaskModel) = addTask(task)

    /** Delete a task */
    fun deleteTask(task: TaskModel) = viewModelScope.launch(Dispatchers.IO) {
        task.id?.let { repo.deleteTask(it) }
    }

    /** Fetch one task by ID (for edit) */
    fun getTaskById(taskId: String): LiveData<TaskModel?> = liveData(Dispatchers.IO) {
        try {
            val snapshot = repo.tasksCol.document(taskId).get().await()
            val task = snapshot.toObject(TaskModel::class.java)?.copy(id = snapshot.id)
            Log.d("UserNewTaskViewModel", "getTaskById  $task")
            emit(task)

        } catch (e: Exception) {
            Log.d("UserNewTaskViewModel", "Error fetching task by ID ${e.message}")
            emit(null)
        }
    }
}