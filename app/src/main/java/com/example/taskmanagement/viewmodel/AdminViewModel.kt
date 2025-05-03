package com.example.taskmanagement.viewmodel


import androidx.databinding.ObservableInt
import androidx.lifecycle.*
import com.example.taskmanagement.model.TaskModel
import com.example.taskmanagement.model.UserModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repo: FirestoreTaskRepository
) : ViewModel() {

    // Expose Firestore users as LiveData
    val allUsers: LiveData<List<UserModel>> = repo.getUsersFlow()
        .asLiveData(Dispatchers.IO)

    private val _userFilter = MutableLiveData<String?>(null)
    private val _statusFilter = MutableLiveData<String?>(null)
    var observerNoRecords: ObservableInt = ObservableInt(0)
    val filteredTasks: LiveData<List<TaskModel>> =
        MediatorLiveData<List<TaskModel>>().apply {
            fun reload() {
                viewModelScope.launch(Dispatchers.IO) {
                    repo.getTasksFlow(_userFilter.value, _statusFilter.value)
                        .collect { postValue(it) }
                }
            }
            addSource(_userFilter)  { reload() }
            addSource(_statusFilter){ reload() }
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
}

