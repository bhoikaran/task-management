package com.example.taskmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanagement.databinding.ActivityLoginBinding
import com.example.taskmanagement.interactors.GeneralListener
import com.example.taskmanagement.model.User
import com.example.taskmanagement.utils.utility.UtilPreference
import com.example.taskmanagement.repository.AuthRepository
import com.example.taskmanagement.viewmodel.LoginViewModel
import com.example.taskmanagement.viewmodel.LoginViewModelFactory


class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    private lateinit var mSharePreference: UtilPreference
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSharePreference = UtilPreference(this)  // make sure this is initialized before use

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.generalListener = generalListener

        val factory = LoginViewModelFactory(AuthRepository())
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
        binding.viewModel = viewModel
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
        viewModel.loginResult.observe(this) { result ->
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
                when (user.role) {
                    "admin" -> startActivity(Intent(this, AdminMainActivity::class.java))
                    "general" -> startActivity(Intent(this, UserMainActivity::class.java))
                }

                  when (user.role) {
                   "admin" -> Toast.makeText(this, "User : Admin", Toast.LENGTH_LONG).show()
                   "general" -> Toast.makeText(this, "User : General", Toast.LENGTH_LONG).show()
               }
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
                viewModel.login()
            }

        }
    }


}
