package com.example.taskmanagement.businesslogic.viewmodel

import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserViewModel(mApplication: MyApplication) : ViewModelBase(mApplication) {

    private val _userFilter = MutableLiveData<String?>(null)
    private val _statusFilter = MutableLiveData<String?>(null)
    var callLogout: MutableLiveData<Void?> = MutableLiveData<Void?>()
    var observerNoRecords: ObservableInt = ObservableInt(0)
    val filteredTasks: LiveData<List<TaskModel>> =
        MediatorLiveData<List<TaskModel>>().apply {
            fun reload() {
                viewModelScope.launch(Dispatchers.IO) {
                    repo.getTasksFlow(mApplication,_userFilter.value, _statusFilter.value)
                        .collect { postValue(it)
                            observerNoRecords.set(if (it.isEmpty()) 2 else 1)
                        }
                }
            }
            addSource(_userFilter) { reload() }
            addSource(_statusFilter) { reload() }
        }

    fun applyFilters(userId: String?, status: String?) {
        _userFilter.value = userId
        _statusFilter.value = status
    }

    fun addTask(task: TaskModel) = viewModelScope.launch(Dispatchers.IO) {
        repo.addOrUpdateTask(task)
    }

    fun updateTask(task: TaskModel) = viewModelScope.launch(Dispatchers.IO) {
        repo.addOrUpdateTask(task)
    }

    fun deleteTask(task: TaskModel) = viewModelScope.launch(Dispatchers.IO) {
        task.id?.let { repo.deleteTask(it) }
    }
    fun logout(){
        repo.clearAllListeners()
        authRepo.signOut()
        mSharePreference?.clear()
        callLogout.value = null
    }
}

