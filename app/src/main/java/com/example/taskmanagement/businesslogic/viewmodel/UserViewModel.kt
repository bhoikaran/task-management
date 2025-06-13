package com.example.taskmanagement.businesslogic.viewmodel

import android.content.Context
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.businesslogic.model.PojoDialogSearch
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.repository.FirestoreTaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserViewModel(mApplication: MyApplication) : ViewModelBase(mApplication) {


    var observableTaskStatus = ObservableString("")
    var selectedStatus: Status = Status.IN_PROGRESS
    var observableSearchDataList: ObservableArrayList<PojoDialogSearch> =
        ObservableArrayList<PojoDialogSearch>()


    private val _userFilter = MutableLiveData<String?>(null)
    private val _statusFilter = MutableLiveData<String?>(null)

    var observerNoRecords: ObservableInt = ObservableInt(0)
    val filteredTasks: LiveData<List<TaskModel>> =
        MediatorLiveData<List<TaskModel>>().apply {
            fun reload() {
                viewModelScope.launch(Dispatchers.IO) {
                    repo.getTasksFlow(mApplication,mSharePreference?.getString(R.string.prefAdminId),_userFilter.value, _statusFilter.value)
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

    fun deleteTask(task: TaskModel) = viewModelScope.launch(Dispatchers.IO) {
        task.id?.let { repo.deleteTask(it) }
    }


    fun addAlertSearchItem(context : Context?) {

        observableSearchDataList.clear()
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

