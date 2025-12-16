package com.example.todolistapp2

import com.example.todolistapp2.TaskList

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update

@Dao
interface TaskListDao {

    @Insert
    suspend fun insert(taskList: TaskList)

    @Query("SELECT * FROM task_lists")
    fun getAllLists(): LiveData<List<TaskList>>

    @Query("SELECT * FROM task_lists WHERE id = :id LIMIT 1")
    suspend fun getListById(id: Int): TaskList?

    @Update
    suspend fun update(taskList: TaskList) // ✅ Add this

    @Delete
    suspend fun delete(taskList: TaskList) // ✅ Add thi
}
