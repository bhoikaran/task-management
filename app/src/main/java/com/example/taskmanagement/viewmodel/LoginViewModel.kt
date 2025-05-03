package com.example.taskmanagement.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.interactors.ObservableString
import com.example.taskmanagement.model.UserModel
import com.example.taskmanagement.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult

    // bound via DataBinding
    var observableEmail: ObservableString = ObservableString("")
    var observablePassword: ObservableString = ObservableString("")

    fun login() {
        val email = observableEmail.trimmed
        val password = observablePassword.trimmed

        Log.d("LoginVM", "Attempting login with email=$email")

        viewModelScope.launch {
            try {
                // This will sign in with Firebase Auth AND fetch the Firestore UserModel
                val userModel = repository.loginAndFetch(email, password)
                _loginResult.value = Result.success(userModel)
            } catch (e: Exception) {
                Log.e("LoginVM", "Login failed", e)
                _loginResult.value = Result.failure(e)
            }
        }
    }
}
