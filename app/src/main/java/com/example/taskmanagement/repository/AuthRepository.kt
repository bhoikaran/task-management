package com.example.taskmanagement.repository

import com.example.taskmanagement.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCol = firestore.collection("users")

    /**
     * Sign up a new user with email/password, then immediately create
     * a matching Firestore document under `users/{uid}`.
     *
     * @return the created FirebaseUser on success, or throws on failure.
     */
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: String = "general"
    ): FirebaseUser {
        // 1) Create in Firebase Auth
        val result = auth
            .createUserWithEmailAndPassword(email, password)
            .await()
        val firebaseUser = result.user
            ?: throw Exception("Auth signup succeeded but user is null")

        // 2) Create Firestore record
        val userModel = UserModel(
            uid = firebaseUser.uid,
            name = name,
            email = email,
            role = role
        )
        usersCol.document(firebaseUser.uid)
            .set(userModel)
            .await()

        return firebaseUser
    }

    /**
     * Log in an existing user with email/password.
     * @return the logged-in FirebaseUser on success, or throws on failure.
     */
    suspend fun login(email: String, password: String): FirebaseUser {
        val result = auth
            .signInWithEmailAndPassword(email, password)
            .await()
        return result.user
            ?: throw Exception("Auth login succeeded but user is null")
    }

    /**
     * Fetch the Firestore user record for the currently logged-in UID.
     * @throws if no document exists or conversion fails.
     */
    suspend fun fetchCurrentUser(): UserModel {
        val uid = auth.currentUser?.uid
            ?: throw Exception("No user is currently signed in")
        val snapshot = usersCol.document(uid).get().await()
        if (!snapshot.exists()) {
            throw Exception("User document not found for UID: $uid")
        }
        // read into model and ensure uid is set
        val u = snapshot.toObject(UserModel::class.java)
            ?: throw Exception("Failed to parse user document")
        return u.copy(uid = snapshot.id)
    }

    /**
     * Convenience: after login(), immediately fetch the UserModel
     */
    suspend fun loginAndFetch(email: String, password: String): UserModel {
        val user = login(email, password)
        return fetchCurrentUser()
    }

    /** Sign out the current user */
    fun signOut() {
        auth.signOut()
    }
}