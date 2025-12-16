package com.example.todolistapp2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val listName: String = "Default",  // ✅ Default to existing list
    val listId: Int, // 🔥 New
)
