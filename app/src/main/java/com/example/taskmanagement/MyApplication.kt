package com.example.taskmanagement

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.taskmanagement.utils.SessionManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth


class MyApplication : Application() {

    lateinit var localBroadcastManager: LocalBroadcastManager

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        if (FirebaseAuth.getInstance().currentUser != null) {
            SessionManager.initSessionListener(this)
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(this)
    }

    /**
     * Checks Internet Connectivity.
     *
     * @return returns true if connected else false.
     */
    fun isInternetConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }


}
