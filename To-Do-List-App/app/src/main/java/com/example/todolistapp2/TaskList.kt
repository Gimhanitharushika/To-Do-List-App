package com.example.todolistapp2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_lists")
data class TaskList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val color: Long = 0L
)


