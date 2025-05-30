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
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.TitleModel
import com.example.taskmanagement.businesslogic.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AdminTaskViewModel(mApplication: MyApplication) : ViewModelBase(mApplication) {
     var taskModel: TaskModel = TaskModel()

    var observableTitle = ObservableString("")
    var observableTaskDescription = ObservableString("")
    var observableTaskAssignTo = ObservableString("")
    var observableTaskAssignToUid = ObservableString("")
    var observableTaskStatus = ObservableString("")
    var selectedStatus :Status = Status.IN_PROGRESS
    var observableAssignDate = ObservableString("")
    var observableCompleteDate = ObservableString("")
    var observableTaskAdminRemark = ObservableString("")
    var observableTaskUserRemark = ObservableString("")

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
    fun setAdminId(uid: String) {
        _adminId.value = uid
    }

    // titles stream:
    val titleSuggestions: LiveData<List<String>> =
        _adminId.switchMap { uid ->
            repo.getTitlesForAdmin(mApplication, uid)
                .asLiveData(viewModelScope.coroutineContext)
        }


    val adminTitles: LiveData<List<TitleModel>> =
        _adminId.switchMap { uid ->
            repo.getAdminTitles(mApplication, uid)
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
            repo.getTasksFlow(context, userFilter.value, statusFilter.value)
                .collect { list ->
                    _filteredTasks.postValue(list)
                    observerNoRecords.set(if (list.isEmpty()) 2 else 1)
                }
        }
    }



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

    fun saveTask() {
        TODO("Not yet implemented")
    }

    fun getUserNameById(userId: String?): String {
        return allUsers.value?.firstOrNull { it.uid == userId }?.name ?: ""
    }
}
