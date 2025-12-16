package com.example.todolistapp2

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

fun backupTaskListsToFirestore(context: Context, taskLists: List<TaskList>) {
    val firestore = FirebaseFirestore.getInstance()
    val collectionRef = firestore.collection("taskLists")

    taskLists.forEach { taskList ->
        val docId = taskList.id.toString()
        collectionRef.document(docId)
            .set(taskList)
            .addOnSuccessListener {
                Toast.makeText(context, "Backed up: ${taskList.name}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed: ${taskList.name}", Toast.LENGTH_SHORT).show()
            }
    }
}
