package com.example.todolistapp2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.todolistapp2.TaskList
import com.example.todolistapp2.TaskListDao
import com.example.todolistapp2.TaskDao
import com.google.firebase.firestore.FirebaseFirestore



class TaskViewModel(
    private val taskDao: TaskDao,
    private val taskListDao: TaskListDao) : ViewModel() {

    val allTaskLists: LiveData<List<TaskList>> = taskListDao.getAllLists()

    fun insertList(taskList: TaskList) {
        viewModelScope.launch {
            taskListDao.insert(taskList)
        }
    }

    fun insert(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun delete(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
    }

    fun update(task: Task) = viewModelScope.launch {
        taskDao.update(task)
    }
    fun getTasksByListId(listId: Int): LiveData<List<Task>> {
        return taskDao.getTasksByListId(listId)
    }

    fun updateList(taskList: TaskList) = viewModelScope.launch {
        taskListDao.update(taskList)
    }

    fun deleteList(taskList: TaskList) = viewModelScope.launch {
        taskListDao.delete(taskList)
    }

    // ✅ Add this method
    suspend fun getListById(id: Int): TaskList? {
        return taskListDao.getListById(id)
    }

    fun backupTaskListsToFirestore(taskLists: List<TaskList>) {
        val firestore = FirebaseFirestore.getInstance()
        val collectionRef = firestore.collection("taskLists")

        taskLists.forEach { taskList ->
            val docId = taskList.id.toString()

            collectionRef.document(docId)
                .set(taskList)
                .addOnSuccessListener {
                    println("Successfully backed up list: ${taskList.name}")
                }
                .addOnFailureListener { e ->
                    println("Failed to back up list: ${taskList.name} - ${e.message}")
                }
        }
    }
}





