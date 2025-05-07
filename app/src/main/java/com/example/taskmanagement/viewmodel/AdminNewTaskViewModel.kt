package com.example.taskmanagement.viewmodel


import android.app.Application
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.*
import com.example.taskmanagement.model.Status
import com.example.taskmanagement.model.TaskModel
import com.example.taskmanagement.model.UserModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AdminNewTaskViewModel(
    private val repo: FirestoreTaskRepository
) : ViewModel() {


    // Expose non-admin users
    val allUsers: LiveData<List<UserModel>> = repo
        .getUsersFlow()
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

    init {
        // Whenever filters change, reload the tasks
        MediatorLiveData<Unit>().apply {
            addSource(userFilter) { loadTasks() }
            addSource(statusFilter) { loadTasks() }
        }
    }



    /** Load tasks from Firestore according to current filters */
    private fun loadTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getTasksFlow(userFilter.value, statusFilter.value)
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
