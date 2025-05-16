package com.example.taskmanagement.businesslogic.viewmodel

import android.text.TextUtils
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModel
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.repository.AuthRepository
import com.example.taskmanagement.repository.FirestoreTaskRepository
import com.example.taskmanagement.utils.utility.UtilPreference

open class ViewModelBase(mApplication: MyApplication) : ViewModel() {

    var observerSnackBarInt: ObservableInt = ObservableInt()
    val observerSnackBarString: ObservableString = ObservableString("")
    var observableProgressBar: ObservableBoolean = ObservableBoolean(false)
    var observerDialogSnackBarInt: ObservableInt = ObservableInt()
    val observerDialogSnackBarString: ObservableString = ObservableString("")
    var observableDialogProgressBar: ObservableBoolean = ObservableBoolean(false)
    val repo = FirestoreTaskRepository()
    val authRepo = AuthRepository()
    var mSharePreference: UtilPreference? = null
    init {
        mSharePreference = UtilPreference(mApplication.applicationContext)
    }

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
}
