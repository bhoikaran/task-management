package com.example.taskmanagement.businesslogic.viewmodel


import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.businesslogic.model.PojoDialogSearch
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AdminViewModel(mApplication: MyApplication) : ViewModelBase(mApplication) {
    var observableTaskAssignTo = ObservableString("")
    var observableTaskAssignToUid = ObservableString("")
    var observableTaskStatus = ObservableString("")
    var selectedStatus: Status = Status.IN_PROGRESS
    var observableSearchDataList: ObservableArrayList<PojoDialogSearch> =
        ObservableArrayList<PojoDialogSearch>()

    // Expose Firestore users as LiveData
    val allUsers: LiveData<List<UserModel>> = repo.getUsersFlow(mApplication, mSharePreference?.getString(R.string.prefAdminId))
        .asLiveData(Dispatchers.IO)

    private val _userFilter = MutableLiveData<String?>(null)
    private val _statusFilter = MutableLiveData<String?>(null)
    var observerNoRecords: ObservableInt = ObservableInt(0)

    val filteredTasks: LiveData<List<TaskModel>> =
        MediatorLiveData<List<TaskModel>>().apply {
            fun reload() {
                viewModelScope.launch(Dispatchers.IO) {
                    repo.getTasksFlow(mApplication,
                        currentUser?.uid,_userFilter.value, _statusFilter.value)
                        .collect { postValue(it) }
                }
            }
            addSource(_userFilter) { reload() }
            addSource(_statusFilter) { reload() }
        }

    fun applyFilters(userId: String?, status: String?) {
        _userFilter.value = userId
        _statusFilter.value = status
    }


    fun deleteTask(task: TaskModel) = viewModelScope.launch(Dispatchers.IO) {
        task.id?.let { repo.deleteTask(it) }
    }


    fun addAlertSearchItem(context : Context?, flag: Int) {

        observableSearchDataList.clear()
        if (flag == 1) {
            val users = allUsers.value
            var pojo : PojoDialogSearch
            if (context != null) {
                pojo = PojoDialogSearch(
                    title = context.getString(R.string.text_all),
                    id ="0",
                    codeValue = "",
                    is_checked = false
                )
                observableSearchDataList.add(pojo)
            }
            users?.forEach { user ->
                 pojo = PojoDialogSearch(
                    title = user.name,
                    id = user.uid,
                    codeValue = user.email,
                    is_checked = false
                )
                observableSearchDataList.add(pojo)
            }

        } else if (flag == 2) {
            var pojo : PojoDialogSearch
            if (context != null) {
                pojo = PojoDialogSearch(
                    title = context.getString(R.string.text_all),
                    id ="0",
                    codeValue = "",
                    is_checked = false
                )
                observableSearchDataList.add(pojo)
            }

            Status.entries.forEach { status ->
                 pojo = PojoDialogSearch(
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

