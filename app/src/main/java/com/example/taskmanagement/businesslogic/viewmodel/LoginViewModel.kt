package com.example.taskmanagement.businesslogic.viewmodel

import android.app.Activity
import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.ObservableString
import com.example.taskmanagement.businesslogic.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


class LoginViewModel(mApplication: MyApplication) : ViewModelBase(mApplication) {
    private val _loginResult = MutableLiveData<Result<UserModel>>()
    val loginResult: LiveData<Result<UserModel>> = _loginResult
    private val auth = FirebaseAuth.getInstance()

    // bound via DataBinding
//    var observableEmail: ObservableString = ObservableString("kbhoi775@gmail.com")
//    var observableEmail: ObservableString = ObservableString("bhoikaran135@gmail.com")
//    var observableEmail: ObservableString = ObservableString("hellobhoikaran@gmail.com")
//    var observablePassword: ObservableString = ObservableString("admin@123")
    var observableEmail: ObservableString = ObservableString("")
    var observablePassword: ObservableString = ObservableString("")
    var isRememberMe: ObservableBoolean = ObservableBoolean(false)
    fun login() {
        observableProgressBar.set(true)
        val email = observableEmail.trimmed
        val password = observablePassword.trimmed

        Log.d("LoginVM", "Attempting login with email=$email")

        viewModelScope.launch {
            try {
                // This will sign in with Firebase Auth AND fetch the Firestore UserModel

                val userModel = authRepo.loginAndFetch(email, password)
                _loginResult.value = Result.success(userModel)
                hideProgressBar()
            } catch (e: Exception) {
                hideProgressBar()
                Log.e("LoginVM", "Login failed", e)
                _loginResult.value = Result.failure(e)
            }
        }
    }


    /**
     * Method responsible for pre-filling saved user credentials into views.
     */
    internal fun prefillRememberMeData() {
        if (mSharePreference!!.getBoolean(R.string.prefIsRememberMe)) {
            isRememberMe.set(true)
            observableEmail.set(mSharePreference!!.getString(R.string.prefUserEmail))
            observablePassword.set(mSharePreference!!.getString(R.string.prefLoginPassword))
        } else{
            observableEmail.set("")
            observablePassword.set("")
            isRememberMe.set(false)
        }

    }


    /**
     * Method responsible for managing remember me feature
     */
    fun checkIsRememberMeEnabled() {
        if (isRememberMe.get()) {
            mSharePreference!!.setBoolean(R.string.prefIsRememberMe, true)
            mSharePreference!!.setString(
                R.string.prefUserEmail,
                observableEmail.trimmed
            )
            mSharePreference!!.setString(
                R.string.prefLoginPassword,
                observablePassword.trimmed
            )

        } else {
            mSharePreference!!.setBoolean(R.string.prefIsRememberMe, false)
        }
    }

    fun sendResetPasswordEmail(activity: Activity) {


        authRepo.sendPasswordResetEmail(activity, observableEmail.trimmed)

    }
}
