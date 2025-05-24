package com.example.taskmanagement.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.GeneralListener
import com.example.taskmanagement.businesslogic.viewmodel.LoginViewModel
import com.example.taskmanagement.databinding.ActivityLoginBinding
import com.example.taskmanagement.utils.utility.UtilPreference
import com.example.taskmanagement.view.activity.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var mSharePreference: UtilPreference
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSharePreference = UtilPreference(this)  // make sure this is initialized before use

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.generalListener = generalListener
        mViewModel = LoginViewModel((applicationContext as MyApplication))
        binding.viewModel = mViewModel
    }

    override fun onResume() {
        super.onResume()
        initState()
        observeData()
    }

    private fun initState() {
      /* binding.emailInput.setText(mSharePreference.getString(R.string.prefEmail));
       binding.passwordInput.setText(mSharePreference.getString(R.string.prefPassword));*/
    }

    private fun observeData() {
        mViewModel.loginResult.observe(this) { result ->
            result.onSuccess { user ->
//                sharedPref.saveUser(user)
                mSharePreference.setString(
                    R.string.prefUserId,
                    user.uid
                )
                mSharePreference.setString(
                    R.string.prefUserName,
                    user.name
                )
                mSharePreference.setString(
                    R.string.prefUserRole,
                    user.role
                )
                startActivity(Intent(this, MainActivity::class.java))
//                startActivity(Intent(this, AdminMainActivity::class.java))
                finish()
            }
            result.onFailure {
                Toast.makeText(this, "Login Failed: ${it.message}", Toast.LENGTH_LONG).show()
                Log.d("Login", "Login Failed: ${it.message}")
            }
        }
    }

    private val generalListener: GeneralListener = GeneralListener { view ->
        when (view?.id) {
            R.id.loginButton -> {
//                mActivity?.navigateToScheduleSession()
                mViewModel.login()
            }

        }
    }


}