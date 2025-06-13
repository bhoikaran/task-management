package com.example.taskmanagement.businesslogic.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.TitleModel
import com.example.taskmanagement.repository.AuthRepository
import com.example.taskmanagement.repository.FirestoreTaskRepository
import com.example.taskmanagement.utils.utility.UtilPreference
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class ViewModelBase(mApplication: MyApplication) : ViewModel() {

//    var observerSnackBarInt: ObservableInt = ObservableInt()
    var observerSnackBarInt: ObservableField<Int> = ObservableField()

    val observerSnackBarString: ObservableString = ObservableString("")
    var observableProgressBar: ObservableBoolean = ObservableBoolean(false)
    var observerDialogSnackBarInt: ObservableInt = ObservableInt()
    val observerDialogSnackBarString: ObservableString = ObservableString("")
    var observableDialogProgressBar: ObservableBoolean = ObservableBoolean(false)
    val repo = FirestoreTaskRepository()
    val authRepo = AuthRepository()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var mSharePreference: UtilPreference? = null
    init {
        mSharePreference = UtilPreference(mApplication.applicationContext)
    }
    var callLogout: MutableLiveData<Void?> = MutableLiveData<Void?>()
    fun handleApiErrorMessage(msg: String?) {
        if (!TextUtils.isEmpty(msg)) {
            observerSnackBarString.set(msg)
        } else {
            onApiFailure()
        }
    }

    fun onApiFailure() {
        observerSnackBarInt.set(com.example.taskmanagement.R.string.message_noconnection)
    }


    /** Create or update a task */
    fun addTask(task: TaskModel) = viewModelScope.launch(Dispatchers.IO) {
        repo.addOrUpdateTask(task)
    }

    fun updateTask(task: TaskModel) = addTask(task)

    fun addTitle(title: TitleModel) = viewModelScope.launch(Dispatchers.IO) {
        repo.addOrUpdateTitle(title)
    }

    fun deleteTitle(titleModel: TitleModel) = viewModelScope.launch(Dispatchers.IO) {
        titleModel.id?.let { repo.deleteTitle(it) }
    }
    fun logout(){
        Log.d("logout","logout")
        repo.clearAllListeners()
        authRepo.signOut()
        mSharePreference?.clear()
        callLogout.value = null
    }
    fun hideProgressBar() {
        observableProgressBar.set(false)
    }
}
