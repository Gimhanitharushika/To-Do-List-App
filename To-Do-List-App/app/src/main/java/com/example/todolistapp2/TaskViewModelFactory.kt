package com.example.todolistapp2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TaskViewModelFactory(
    private val taskDao: TaskDao,
    private val taskListDao: TaskListDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDao, taskListDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

