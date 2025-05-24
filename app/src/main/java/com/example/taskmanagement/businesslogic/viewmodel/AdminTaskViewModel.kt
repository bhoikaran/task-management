package com.example.taskmanagement.businesslogic.viewmodel

import android.content.Context
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AdminTaskViewModel(mApplication: MyApplication) : ViewModelBase(mApplication) {


    // Expose non-admin users
    val allUsers: LiveData<List<UserModel>> = repo
        .getUsersFlow(mApplication)
        .asLiveData(Dispatchers.IO)

    // Status list
    val allStatus: List<Status> = Status.values().toList()

    // Filtered tasks
    private val _filteredTasks = MutableLiveData<List<TaskModel>>()
    val filteredTasks: LiveData<List<TaskModel>> = _filteredTasks

    // UI state
    var observerNoRecords = ObservableInt(0)
    var editMode = ObservableBoolean(false)

    private val userFilter = MutableLiveData<String?>(null)
    private val statusFilter = MutableLiveData<String?>(null)
    private val _adminId = MutableLiveData<String>()
    fun setAdminId(uid: String) { _adminId.value = uid }

    // titles stream:
    val titleSuggestions: LiveData<List<String>> =
        _adminId.switchMap { uid ->
            repo.getTitlesForAdmin(mApplication,uid)
                .asLiveData(viewModelScope.coroutineContext)
        }
    init {
        // Whenever filters change, reload the tasks
        MediatorLiveData<Unit>().apply {
            addSource(userFilter) { loadTasks(mApplication) }
            addSource(statusFilter) { loadTasks(mApplication) }
        }
    }



    /** Load tasks from Firestore according to current filters */
    private fun loadTasks(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getTasksFlow(context,userFilter.value, statusFilter.value)
                .collect { list ->
                    _filteredTasks.postValue(list)
                    observerNoRecords.set(if (list.isEmpty()) 2 else 1)
                }
        }
    }


    /** Create or update a task */
    fun addTask(task: TaskModel) = viewModelScope.launch(Dispatchers.IO) {
        repo.addOrUpdateTask(task)
    }

    fun updateTask(task: TaskModel) = addTask(task)


    /** Fetch one task by ID (for edit) */
    fun getTaskById(taskId: String): LiveData<TaskModel?> = liveData(Dispatchers.IO) {
        try {
            val snapshot = repo.tasksCol.document(taskId).get().await()
            val task = snapshot.toObject(TaskModel::class.java)?.copy(id = snapshot.id)
            emit(task)
        } catch (e: Exception) {
            emit(null)
        }
    }
}
