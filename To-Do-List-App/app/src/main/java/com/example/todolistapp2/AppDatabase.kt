
package com.example.todolistapp2

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.todolistapp2.Task
import com.example.todolistapp2.TaskDao
import com.example.todolistapp2.TaskList
import com.example.todolistapp2.TaskListDao

@Database(entities = [Task::class, TaskList::class], version = 4)

abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun taskListDao(): TaskListDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_database"
                )
                    .fallbackToDestructiveMigration() // 🔥 Add this line here
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
