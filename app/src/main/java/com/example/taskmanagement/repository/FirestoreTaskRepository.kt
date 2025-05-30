package com.example.taskmanagement.repository


import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.taskmanagement.businesslogic.model.Status
import com.example.taskmanagement.businesslogic.model.TaskModel
import com.example.taskmanagement.businesslogic.model.TitleModel
import com.example.taskmanagement.businesslogic.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreTaskRepository {
    private val db = FirebaseFirestore.getInstance()
    internal val tasksCol = db.collection("tasks")
    private val usersCol = db.collection("users")
    private val titleCol = db.collection("titles")

    // Track listeners
    private val activeListeners = mutableListOf<ListenerRegistration>()

    fun getUsersFlow(context : Context): Flow<List<UserModel>> = callbackFlow {
        val sub = usersCol.addSnapshotListener { snap, ex ->
            Log.d("getUsersFlow", "addSnapshotListener: ${snap?.documents}")
            if (ex != null) {
                // 👇 Handle permission/firestore denied error
                Toast.makeText(context, "Something Went Wrong...", Toast.LENGTH_SHORT).show()
                Log.e("getUsersFlow", "Firestore error", ex)
                // Optional: send empty list or keep last state
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snap!!.documents.mapNotNull {
                it.toObject(UserModel::class.java)?.copy(uid = it.id)
            }.filter { it.role != "admin" }
            trySend(list)
        }
        activeListeners.add(sub) // 👈 Track listener
        awaitClose {
            sub.remove()
            activeListeners.remove(sub)
        }
    }

    fun getTasksFlow(context: Context, userId: String?, status: String?): Flow<List<TaskModel>> = callbackFlow {
        var query: Query = tasksCol
        userId?.let { query = query.whereEqualTo("assignPersonId", it) }
        status?.let { query = query.whereEqualTo("status", it) }
        query = query.orderBy("assignDate", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snap, ex ->
            Log.d("getTasksFlow", "addSnapshotListener: ${snap?.documents}")
            if (ex != null) {
                Toast.makeText(context, "Something Went Wrong...", Toast.LENGTH_SHORT).show()
                Log.e("getTasksFlow", "Firestore error", ex)
                trySend(emptyList())
                return@addSnapshotListener
            }

            val list = snap!!.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                val rawAssign = data["assignDate"]
                val assignMillis = when (rawAssign) {
                    is com.google.firebase.Timestamp -> rawAssign.toDate().time
                    is Number                        -> rawAssign.toLong()
                    else                             -> 0L
                }

                val rawComplete = data["completionDate"]
                val completeMillis = when (rawComplete) {
                    is com.google.firebase.Timestamp -> rawComplete.toDate().time
                    is Number                        -> rawComplete.toLong()
                    else                             -> null
                }

                TaskModel(
                    id              = doc.id,
                    title           = data["title"] as? String ?: "",
                    taskDetail      = data["taskDetail"] as? String ?: "",
                    assignPersonId  = data["assignPersonId"] as? String ?: "",
                    assignDate      = assignMillis,
                    completionDate  = completeMillis,
                    remark          = data["remark"] as? String,
                    userRemark      = data["userRemark"] as? String,
                    status          = Status.valueOf(data["status"] as? String ?: Status.IN_PROGRESS.name),
                    createdBy       = data["createdBy"] as? String ?: ""
                )
            }
            trySend(list)
        }

        activeListeners.add(listener) // 👈 Track listener
        awaitClose {
            listener.remove()
            activeListeners.remove(listener)
        }
    }

    // Call this on logout
    fun clearAllListeners() {
        activeListeners.forEach { it.remove() }
        activeListeners.clear()
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
    suspend fun addOrUpdateTitle(title: TitleModel) {
        if (title.id.isNullOrEmpty()) {
            titleCol.add(title).await()
        } else {
            tasksCol.document(title.id).set(title).await()
        }
    }

    suspend fun deleteTitle(id: String) {
        titleCol.document(id).delete().await()
    }

    fun getTitlesForAdmin(context : Context,adminUid: String): Flow<List<String>> = callbackFlow {
        val listener = FirebaseFirestore.getInstance()
            .collection("titles")
            .whereEqualTo("createdBy", adminUid)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // 👇 Handle permission/firestore denied error
                    Toast.makeText(context, "Something Went Wrong...", Toast.LENGTH_SHORT).show()
                    Log.e("getUsersFlow", "Firestore error", error)
                    // Optional: send empty list or keep last state
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val titles = value?.documents?.mapNotNull { it.getString("name") } ?: emptyList()
                trySend(titles)
            }

        awaitClose { listener.remove() }
    }

    suspend fun saveNewTitleIfNotExists(title: String, adminUid: String) {
        val titlesRef = FirebaseFirestore.getInstance().collection("titles")
        val existing = titlesRef
            .whereEqualTo("createdBy", adminUid)
            .whereEqualTo("name", title)
            .get()
            .await()

        if (existing.isEmpty) {
            titlesRef.add(
                mapOf(
                    "name" to title,
                    "createdBy" to adminUid
                )
            )
        }
    }

   /* fun fetchTitlesForCurrentUser(onResult: (List<TitleModel>) -> Unit, onError: (Exception) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("titles")
            .whereEqualTo("createdBy", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val titles = snapshot.toObjects(TitleModel::class.java)
                onResult(titles)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }*/

    fun getAdminTitles(context: Context, adminId: String): Flow<List<TitleModel>> = callbackFlow {
        val listener = FirebaseFirestore.getInstance()
            .collection("titles")
            .whereEqualTo("createdBy", adminId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val titles = snapshot?.toObjects(TitleModel::class.java) ?: emptyList()
                trySend(titles)
            }

        awaitClose { listener.remove() }
    }




}
