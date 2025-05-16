package com.example.taskmanagement.businesslogic.viewmodel


import android.util.Log
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AdminViewModel(mApplication: MyApplication) : ViewModelBase(mApplication) {
    // Expose Firestore users as LiveData
    val allUsers: LiveData<List<UserModel>> = repo.getUsersFlow(mApplication)
        .asLiveData(Dispatchers.IO)
    var callLogout: MutableLiveData<Void?> = MutableLiveData<Void?>()
    private val _userFilter = MutableLiveData<String?>(null)
    private val _statusFilter = MutableLiveData<String?>(null)
    var observerNoRecords: ObservableInt = ObservableInt(0)
    val filteredTasks: LiveData<List<TaskModel>> =
        MediatorLiveData<List<TaskModel>>().apply {
            fun reload() {
                Log.d("filteredTasks", "filteredTasks+++ : " + filteredTasks.value)
                viewModelScope.launch(Dispatchers.IO) {
                    repo.getTasksFlow(mApplication,_userFilter.value, _statusFilter.value)
                        .collect { postValue(it) }
                }
                Log.d("filteredTasks", "filteredTasks+++ : " + filteredTasks.value)
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

