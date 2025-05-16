package com.example.taskmanagement.businesslogic.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.businesslogic.model.UserModel
import kotlinx.coroutines.launch


class LoginViewModel(mApplication: MyApplication) : ViewModelBase(mApplication) {
    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    // bound via DataBinding
    var observableEmail: ObservableString = ObservableString("kbhoi775@gmail.com")
//    var observableEmail: ObservableString = ObservableString("bhoikaran135@gmail.com")
//    var observableEmail: ObservableString = ObservableString("hellobhoikaran@gmail.com")
    var observablePassword: ObservableString = ObservableString("admin@123")

    fun login() {
        val email = observableEmail.trimmed
        val password = observablePassword.trimmed

        Log.d("LoginVM", "Attempting login with email=$email")

        viewModelScope.launch {
            try {
                // This will sign in with Firebase Auth AND fetch the Firestore UserModel
                val userModel = authRepo.loginAndFetch(email, password)
                _loginResult.value = Result.success(userModel)
            } catch (e: Exception) {
                Log.e("LoginVM", "Login failed", e)
                _loginResult.value = Result.failure(e)
            }
        }
    }
}
