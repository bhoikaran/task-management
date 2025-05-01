package com.example.taskmanagement.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.taskmanagement.network.UserDao
import com.example.taskmanagement.model.User
import com.example.taskmanagement.model.Task
import com.example.taskmanagement.network.TaskDao
import com.example.taskmanagement.utils.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// AppDatabase.kt
@Database(entities = [Task::class, User::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) { // Corrected here
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).userDao()
                                prepopulateUsers(dao)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateUsers(userDao: UserDao) {
            val defaultUsers = listOf(
                User(name = "OP"),
                User(name = "JJ"),
                User(name = "MM"),
                User(name = "BS"),
                User(name = "KV"),
                User(name = "AK"),
            )
            defaultUsers.forEach { userDao.insertUser(it) }
        }
    }
}

