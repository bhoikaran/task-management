package com.example.taskmanagement.repository


import com.example.taskmanagement.model.TaskModel
import com.example.taskmanagement.model.UserModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreTaskRepository {
    private val db = FirebaseFirestore.getInstance()
    private val tasksCol = db.collection("tasks")
    private val usersCol = db.collection("users")

    fun getUsersFlow(): Flow<List<UserModel>> = callbackFlow {
        val sub = usersCol.addSnapshotListener { snap, ex ->
            if (ex != null) { close(ex); return@addSnapshotListener }
            val list = snap!!.documents.mapNotNull { it.toObject(UserModel::class.java)?.copy(uid = it.id) }
            trySend(list)
        }
        awaitClose { sub.remove() }
    }

    fun getTasksFlow(userId: String?, status: String?): Flow<List<TaskModel>> = callbackFlow {
        var query: Query = tasksCol
        userId?.let { query = query.whereEqualTo("assignPersonId", it) }
        status?.let { query = query.whereEqualTo("status", it) }
        query = query.orderBy("assignDate", Query.Direction.DESCENDING)

        val sub = query.addSnapshotListener { snap, ex ->
            if (ex != null) { close(ex); return@addSnapshotListener }
            val list = snap!!.documents.mapNotNull { it.toObject(TaskModel::class.java) }
            trySend(list)
        }
        awaitClose { sub.remove() }
    }

    suspend fun addOrUpdateTask(task: TaskModel) {
        if (task.id.isNullOrEmpty()) {
            tasksCol.add(task).await()
        } else {
            tasksCol.document(task.id).set(task).await()
        }
    }

    suspend fun deleteTask(id: String) {
        tasksCol.document(id).delete().await()
    }
}

