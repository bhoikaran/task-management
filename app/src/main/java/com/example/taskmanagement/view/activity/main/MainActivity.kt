package com.example.taskmanagement.view.activity.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.fragment.app.Fragment
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.viewmodel.ViewModelMain
import com.example.taskmanagement.databinding.ActivityMainBinding
import com.example.taskmanagement.view.activity.BaseActivity
import com.example.taskmanagement.view.activity.LoginActivity
import com.example.taskmanagement.view.fragments.AdminHomeFragment
import com.example.taskmanagement.view.fragments.UserHomeFragment

class MainActivity : BaseActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private var mActionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var mViewModel: ViewModelMain? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->

                    Log.d("MainActivity", "onCreate: ${doc.data}")
                    val role = doc.getString("role")
                    when (role) {
                        "admin" -> loadFragment(AdminHomeFragment())
                        "general" -> loadFragment(UserHomeFragment())
                        else -> Toast.makeText(this, "Invalid role", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    fun addFragment(fragment: Fragment?, tag: String?) {

        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment!!)
            .addToBackStack(tag).commit()
    }

    fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

}


