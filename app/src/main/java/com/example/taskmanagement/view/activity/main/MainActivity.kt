package com.example.taskmanagement.view.activity.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.taskmanagement.MyApplication
import com.example.taskmanagement.R
import com.example.taskmanagement.businesslogic.interactors.BackPressHandler
import com.example.taskmanagement.businesslogic.viewmodel.ViewModelMain
import com.example.taskmanagement.databinding.ActivityMainBinding
import com.example.taskmanagement.view.activity.BaseActivity
import com.example.taskmanagement.view.activity.LoginActivity
import com.example.taskmanagement.view.fragments.AdminHomeFragment
import com.example.taskmanagement.view.fragments.FragmentAdminTask
import com.example.taskmanagement.view.fragments.FragmentUserTask
import com.example.taskmanagement.view.fragments.UserHomeFragment

class MainActivity : BaseActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private var mActionBarDrawerToggle: ActionBarDrawerToggle? = null
    private lateinit var mViewModel: ViewModelMain
    private var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mViewModel = ViewModelMain(this.applicationContext as MyApplication)
        initComponents()
        observeEvents()
        val user = auth.currentUser
        user?.let {
            val uid = it.uid
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    val serverSessionId = doc.getString("sessionId")
                    val localSessionId =
                        mViewModel.mSharePreference?.getString(R.string.prefSession)
                    if (serverSessionId != localSessionId) {
                        // Session mismatch => logged in elsewhere
                        mViewModel.logout()
                        // Optionally show dialog and navigate to login
                    } else {
                        val role = doc.getString("role")
                        when (role) {
                            "admin" -> loadFragment(AdminHomeFragment())
                            "general" -> loadFragment(UserHomeFragment())
                            else -> Toast.makeText(this, "Invalid role", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
        }
    }

    private fun observeEvents() {

        mViewModel.callLogout.observe(
            this,
            { it ->
                mViewModel.mSharePreference?.clear()
                navigateToLogin()
            })

    }

    private fun initComponents() {

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showConfirmationDialog((getString(R.string.text_logout))) { dialog, which ->
                    Log.d("logout", "logout showConfirmationDialog")
                    mViewModel.logout()
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getCurrentFragment(): Fragment {
        return supportFragmentManager.findFragmentById(R.id.fragment_container)!!
    }

    /*override fun onBackPressed() {
        Log.d("onBackPressed","onBackPressed")
        Log.d("onBackPressed","onBackPressed backStackEntryCount : ${supportFragmentManager.backStackEntryCount}")
        if (supportFragmentManager.backStackEntryCount > 0) {
            if (getCurrentFragment() != null) {
                val fragment: Fragment = getCurrentFragment()
                Log.d("onBackPressed","onBackPressed fragment : $fragment")
                if (fragment is FragmentAdminTask) {
                    if ((fragment as BackPressHandler).onBackPressed()) {
                        return
                    }
                    return
                } else if (fragment is FragmentUserTask) {
                    if ((fragment as BackPressHandler).onBackPressed()) {
                        return
                    }
                    return
                }

            }
        } else {
            Log.d("onBackPressed","onBackPressed else ")
            if (doubleBackToExitPressedOnce) {
                Log.d("onBackPressed","onBackPressed else $doubleBackToExitPressedOnce")
                finish()
                return
            }
            Log.d("onBackPressed","onBackPressed else svsdsgg")
            this.doubleBackToExitPressedOnce = true
            mViewModel.observerSnackBarInt.set(R.string.msg_exit_app)
            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2500)

            super.onBackPressed()
        }
    }*/


    override fun onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {

            if (getCurrentFragment() != null) {
                var fragment = getCurrentFragment()
                if (fragment is FragmentAdminTask) {
                    if ((fragment as BackPressHandler).onBackPressed()) {
                        return
                    }
                    return
                } else if (fragment is FragmentUserTask) {
                    if ((fragment as BackPressHandler).onBackPressed()) {
                        return
                    }
                    return
                }

                super.onBackPressed()
                fragment = getCurrentFragment()

            }
        } else {
            if (doubleBackToExitPressedOnce) {
                finish()
                return
            }

            this.doubleBackToExitPressedOnce = true
            mViewModel.observerSnackBarInt.set(R.string.msg_exit_app)
            mViewModel.observerSnackBarString.set(getString(R.string.msg_exit_app))
            Toast.makeText(this, getString(R.string.msg_exit_app), Toast.LENGTH_SHORT).show()

            Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2500)

//            super.onBackPressed()
        }
    }


}


