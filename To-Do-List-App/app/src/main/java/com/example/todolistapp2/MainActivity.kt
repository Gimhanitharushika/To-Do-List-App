package com.example.todolistapp2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolistapp2.ui.theme.ToDoListApp2Theme
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.widget.Toast

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(database.taskDao(), database.taskListDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this) // ✅ Initialize Firebase

        setContent {
            ToDoListApp2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        taskViewModel = taskViewModel,
                        onListSelected = { listId ->
                            val intent = Intent(this, ListActivity::class.java)
                            intent.putExtra("listId", listId)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel,
    onListSelected: (Int) -> Unit
) {
    val taskLists by taskViewModel.allTaskLists.observeAsState(emptyList())
    var newListName by remember { mutableStateOf("") }

    var listToEdit by remember { mutableStateOf<TaskList?>(null) }
    var editedListName by remember { mutableStateOf("") }

    var searchQuery by remember { mutableStateOf("") }
    // Filter task lists based on search query (case-insensitive)
    val searchResults = if (searchQuery.isNotBlank()) {
        taskLists.filter { it.name.contains(searchQuery, ignoreCase = true) }
    } else {
        emptyList()
    }

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Gimhani’s TO DO List!",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("This App was developed by Gimhani Tharushika (D/BCE/23/0015)")

            Spacer(modifier = Modifier.height(30.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Task Lists") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔍 Show search results if there's a query
            if (searchQuery.isNotBlank()) {
                Text("Search Results:", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (searchResults.isEmpty()) {
                    Text("Search not found", color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    searchResults.forEach { list ->
                        Button(
                            onClick = { onListSelected(list.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB39DDB), // lighter purple for search results
                                contentColor = Color.Black
                            )
                        ) {
                            Text(list.name)
                        }
                    }
                }

                Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            }


            // Input for new list
            OutlinedTextField(
                value = newListName,
                onValueChange = { newListName = it },
                label = { Text("Enter New List Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = {
                if (newListName.isNotBlank()) {
                    taskViewModel.insertList(TaskList(id = 0, name = newListName, color = 0L))
                    newListName = ""
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Create New List")
            }

            Spacer(modifier = Modifier.height(24.dp))

            //Backup button
            Button(onClick = {
                backupTaskListsToFirestore(context, taskLists)
            }) {
                Text("Backup Lists to Cloud")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Restore button (NEW)
            RestoreButton(taskViewModel = taskViewModel)

            Spacer(modifier = Modifier.height(24.dp))

            Text("Your Task Lists:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Show all lists
            taskLists.forEach { list ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFF9364D7))
                        .combinedClickable(
                            onClick = { onListSelected(list.id) },
                            onLongClick = {
                                listToEdit = list
                                editedListName = list.name
                            }

                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = list.name,
                        color = Color.Black,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }

        //Alert Dialog
        if (listToEdit != null) {
            AlertDialog(
                onDismissRequest = {
                    listToEdit = null
                    editedListName = ""
                },
                title = { Text("Edit Task List") },
                text = {
                    OutlinedTextField(
                        value = editedListName,
                        onValueChange = { editedListName = it },
                        label = { Text("List Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            listToEdit = null
                            editedListName = ""
                        }) {
                            Text("Cancel")
                        }

                        TextButton(onClick = {
                            listToEdit?.let {
                                if (editedListName.isNotBlank()) {
                                    val updatedList = it.copy(name = editedListName)
                                    taskViewModel.updateList(updatedList)
                                }
                            }
                            listToEdit = null
                            editedListName = ""
                        }) {
                            Text("Save")
                        }
                    }
                },
                dismissButton = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                listToEdit?.let { taskViewModel.deleteList(it) }
                                listToEdit = null
                                editedListName = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete List", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            )
        }

    }
// 🔹 RestoreButton Composable (OUTSIDE HomeScreen)
@Composable
fun RestoreButton(taskViewModel: TaskViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Button(onClick = {
        val firestore = FirebaseFirestore.getInstance()
        val collectionRef = firestore.collection("taskLists")

        collectionRef.get()
            .addOnSuccessListener { querySnapshot ->
                scope.launch {
                    var addedCount = 0
                    var skippedCount = 0

                    for (document in querySnapshot.documents) {
                        val taskList = document.toObject(TaskList::class.java)
                        if (taskList != null) {
                            val existingList = taskViewModel.getListById(taskList.id)
                            if (existingList == null) {
                                taskViewModel.insertList(taskList)
                                addedCount++
                            } else {
                                skippedCount++
                            }
                        }
                    }

                    Toast.makeText(
                        context,
                        "Restore complete: $addedCount added, $skippedCount skipped",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }) {
        Text("Restore Lists from Cloud")
    }
}



