package com.example.taskmanagement.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.databinding.DataBindingUtil
import com.example.taskmanagement.R
import com.example.taskmanagement.databinding.ActivitySplashBinding
import com.example.taskmanagement.view.activity.main.MainActivity

class ActivitySplash : BaseActivity() {

    private lateinit var mBinding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val next = if (auth.currentUser == null) {
                LoginActivity::class.java
            } else {
                // optionally read saved role and pick Admin or User
                MainActivity::class.java
            }
            startActivity(Intent(this, next))
            finish()
        }, 1500) // 1.5s delay

    }
}
