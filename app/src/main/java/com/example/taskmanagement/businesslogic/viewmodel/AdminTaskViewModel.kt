package com.example.taskmanagement.businesslogic.viewmodel

import android.content.Context
import android.util.Log
import androidx.databinding.ObservableArrayList
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
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.businesslogic.model.PojoDialogSearch
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.TitleModel
import com.example.taskmanagement.businesslogic.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AdminTaskViewModel(mApplication: MyApplication) : ViewModelBase(mApplication) {
    var taskModel: TaskModel = TaskModel()
    var observableBottomSheetTitle: ObservableString = ObservableString("")
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
    var observableSearchDataList: ObservableArrayList<PojoDialogSearch> =
        ObservableArrayList<PojoDialogSearch>()
    lateinit var  users: ObservableArrayList<UserModel>

    // Expose non-admin users
    val allUsers: LiveData<List<UserModel>> = repo
        .getUsersFlow(mApplication, mSharePreference?.getString(R.string.prefAdminId))
        .asLiveData(Dispatchers.IO)

    // Status list
    val allStatus: List<Status> = Status.entries

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
        MediatorLiveData<Unit>().apply {
            addSource(userFilter) { loadTasks(mApplication) }
            addSource(statusFilter) { loadTasks(mApplication) }
        }
    }


    /** Load tasks from Firestore according to current filters */
    private fun loadTasks(context: Context) {
        observableProgressBar.set(true)
        viewModelScope.launch(Dispatchers.IO) {


            repo.getTasksFlow(context, currentUser?.uid, userFilter.value, statusFilter.value)
                .collect { list ->
                    _filteredTasks.postValue(list)
                    observerNoRecords.set(if (list.isEmpty()) 2 else 1)
                    observableProgressBar.set(false)
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

    fun addAlertSearchItem(flag: Int) {

        observableSearchDataList.clear()
        if (flag == 1) {
            val users = allUsers.value
            users?.forEach { user ->
                val pojo = PojoDialogSearch(
                    title = user.name,
                    id = user.uid,
                    codeValue = user.email,
                    is_checked = false
                )
                Log.d("Tag", "observableSearchDataList : ${user.name}")
                observableSearchDataList.add(pojo)
            }
            Log.d("Tag", "observableSearchDataList : ${observableSearchDataList.size}")

        } else if (flag == 2) {
            Status.entries.forEach { status ->
                val pojo = PojoDialogSearch(
                    title = status.name.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    id = status.name,
                    codeValue = "",
                    is_checked = false
                )
                observableSearchDataList.add(pojo)
            }
        }

    }
}
