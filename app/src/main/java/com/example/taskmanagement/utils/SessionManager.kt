package com.example.taskmanagement.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.taskmanagement.view.activity.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import androidx.core.content.edit
import com.example.taskmanagement.R
import com.example.taskmanagement.repository.FirestoreTaskRepository
import com.example.taskmanagement.utils.utility.UtilPreference

object SessionManager {
    private var listenerRegistration: ListenerRegistration? = null
    private val repo = FirestoreTaskRepository()
    private val auth = FirebaseAuth.getInstance()

    fun initSessionListener(context: Context) {

        var mSharePreference = UtilPreference(context.applicationContext)
        listenerRegistration?.remove() // prevent duplicates
        val user = auth.currentUser ?: return
        val sessionId = mSharePreference.getString(R.string.prefSession)
        val docRef = repo.usersCol.document(user.uid)

        listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            Log.d("SessionManager", "initSessionListener: ${snapshot.data}")
            val remoteSessionId = snapshot.getString("sessionId")
            if (remoteSessionId != sessionId) {
                // Session mismatch => logout
                FirebaseAuth.getInstance().signOut()
                listenerRegistration?.remove()

                // Redirect to login (optional)
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        }
    }


    fun clear() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}
