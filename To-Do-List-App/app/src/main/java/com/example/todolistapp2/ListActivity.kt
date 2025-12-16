package com.example.todolistapp2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolistapp2.ui.theme.ToDoListApp2Theme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import org.burnoutcrew.reorderable.*
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.graphics.Color

class ListActivity : ComponentActivity() {

    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize ViewModel here using your factory
        val database = AppDatabase.getDatabase(applicationContext)
        val factory = TaskViewModelFactory(database.taskDao(), database.taskListDao())
        taskViewModel = ViewModelProvider(this, factory)[TaskViewModel::class.java]

        val listId = intent.getIntExtra("listId", -1) // ✅ Added
        if (listId == -1) {
            // Handle error: Show message or finish activity to avoid crash
            finish()
            return
        }

        setContent {
            ToDoListApp2Theme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    TaskListScreen(
                        modifier = Modifier.padding(innerPadding),
                        taskViewModel = taskViewModel,
                                listId = listId // ✅ Pass listId to screen
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskListScreen(
    modifier: Modifier = Modifier,
    taskViewModel: TaskViewModel,
    listId: Int // ✅ Accept listId
) {
    val taskList by taskViewModel.getTasksByListId(listId).observeAsState(emptyList())
    var task by remember { mutableStateOf("") }

    // ✅ Edit State (MODIFIED)
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var editedTaskName by remember { mutableStateOf("") }

    // 👇 Remember reordered list locally
    val reorderedList = remember { mutableStateListOf<Task>() }
    LaunchedEffect(taskList) {
        reorderedList.clear()
        reorderedList.addAll(taskList)
    }
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        reorderedList.add(to.index, reorderedList.removeAt(from.index))
    })

    // ✨ Added for sort options
    var expanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Default") }

// ✅ Update reorderedList when taskList or sortOption changes
    LaunchedEffect(taskList, sortOption) {
        reorderedList.clear()
        reorderedList.addAll(
            when (sortOption) {
                "Alphabetical" -> taskList.sortedBy { it.name.lowercase() }
                else -> taskList
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("My To-Do List", fontSize = 22.sp)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = task,
                onValueChange = { task = it },
                label = { Text("Enter new task") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (task.isNotBlank()) {
                        // Insert new task with current timestamp
                        taskViewModel.insert(Task(name = task, listId = listId))
                        task = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Task")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✨ Dropdown for sort option
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Button(onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0B90F5), // Background color of the button
                        contentColor = Color.Black          // Text color inside the button
                    )
                ) {
                    Text("Sort: $sortOption")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Default (Manual Order)") },
                        onClick = {
                            sortOption = "Default"
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Alphabetical (A-Z)") },
                        onClick = {
                            sortOption = "Alphabetical"
                            expanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // <-- FIXED HERE: Use LazyColumn (NOT ReorderableLazyColumn)
            LazyColumn(
                state = state.listState,
                modifier = Modifier
                    .fillMaxSize()
                    .reorderable(state) // attach reorderable modified
            ) {
                // Use reorderedList so UI updates on reorder
                items(reorderedList, key = { it.id }) { item ->
                    // Wrap each item with ReorderableItem
                    ReorderableItem(state = state, key = item.id) { isDragging ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                // 🔁 Only allow drag if Default mode
                                .then(
                                    if (sortOption == "Default")
                                        Modifier.detectReorderAfterLongPress(state)
                                    else Modifier
                                )
                                .detectReorderAfterLongPress(state) // ✅ Enables dragging
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            taskToEdit = item
                                            editedTaskName = item.name
                                        },

                                        )
                                }
                        ) {
                            // ✅ This is the drag handle
                            Text(
                                text = "☰",
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .reorderable(state)  // ✅ Enables drag
                            )
                            Text(
                                text = "• ${item.name}",
                                fontSize = 18.sp,
                                modifier = Modifier
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            }

            // ✅ Edit Dialog (NEW)
            if (taskToEdit != null) {
                AlertDialog(
                    onDismissRequest = {
                        taskToEdit = null
                        editedTaskName = ""
                    },
                    title = { Text("Edit Task") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editedTaskName,
                                onValueChange = { editedTaskName = it },
                                label = { Text("Task Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = {
                                taskToEdit = null
                                editedTaskName = ""
                            }) {
                                Text("Cancel")
                            }

                            TextButton(onClick = {
                                taskToEdit?.let {
                                    if (editedTaskName.isNotBlank()) {
                                        val updatedTask = it.copy(name = editedTaskName)
                                        taskViewModel.update(updatedTask)
                                    }
                                }
                                taskToEdit = null
                                editedTaskName = ""
                            }) {
                                Text("Save")
                            }
                        }
                    },
                    dismissButton = {
                        // Delete button placed AFTER Save/Cancel
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    taskToEdit?.let { taskViewModel.delete(it) }
                                    taskToEdit = null
                                    editedTaskName = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Delete Task", color = MaterialTheme.colorScheme.onError)
                            }
                        }
                    }
                )
            }
        }
    }
}





