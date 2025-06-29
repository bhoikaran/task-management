package com.example.taskmanagement.businesslogic.viewmodel

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class UserTaskViewModel(mApplication: MyApplication) : ViewModelBase(mApplication) {




    var observableTitle = ObservableString("")
    var observableTaskDescription = ObservableString("")
    var observableTaskAssignTo = ObservableString("")
    var observableTaskAssignToUid = ObservableString("")
    var observableTaskStatus = ObservableString("")
    var selectedStatus: Status = Status.IN_PROGRESS
    var observableAssignDate = ObservableString("")
    var observableCompleteDate = ObservableString("")
    var observableTaskAdminRemark = ObservableString("")
    var observableTaskUserRemark = ObservableString("")


    // Filtered tasks
    private val _filteredTasks = MutableLiveData<List<TaskModel>>()
    val filteredTasks: LiveData<List<TaskModel>> = _filteredTasks

    // UI state
    var observerNoRecords = ObservableInt(0)
    var editMode = ObservableBoolean(false)


    /** Create or update a task */

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