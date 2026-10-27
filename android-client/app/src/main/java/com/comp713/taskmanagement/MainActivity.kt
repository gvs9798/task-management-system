package com.comp713.taskmanagement

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.comp713.taskmanagement.ui.theme.TaskManagementMobileTheme
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TaskManagementMobileTheme {
                TaskManagementScreen()
            }
        }
    }
}

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val status: String,
    val userId: Int
)

@Composable
fun TaskManagementScreen() {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var message by remember {
        mutableStateOf("Press Load Tasks to retrieve tasks from the server.")
    }

    var tasks by remember {
        mutableStateOf<List<Task>>(emptyList())
    }

    fun refreshTasks() {

        message = "Loading tasks..."

        loadTasks(
            onSuccess = { loadedTasks ->

                tasks = loadedTasks

                message =
                    if (loadedTasks.isEmpty()) {
                        "No tasks found."
                    } else {
                        ""
                    }
            },
            onError = { error ->
                message = error
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Task Management",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Android Client",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = {
                Text("Task Title")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = {
                Text("Description")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                if (title.isBlank() || description.isBlank()) {

                    message = "Please enter a title and description."

                } else {

                    message = "Creating task..."

                    createTask(
                        title = title,
                        description = description,
                        onResult = { success, result ->

                            message = result

                            if (success) {
                                title = ""
                                description = ""
                                refreshTasks()
                            }
                        }
                    )
                }
            }
        ) {
            Text("Create Task")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                refreshTasks()
            }
        ) {
            Text("Load Tasks")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (message.isNotBlank()) {

            Text(
                text = message,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        tasks.forEach { task ->

            TaskCard(
                task = task,
                onComplete = {

                    message = "Updating task..."

                    updateTaskStatus(
                        task = task,
                        newStatus = "DONE",
                        onResult = { success, result ->

                            message = result

                            if (success) {
                                refreshTasks()
                            }
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onComplete: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Status: ${task.status}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Task ID: ${task.id}",
                style = MaterialTheme.typography.bodySmall
            )

            if (!task.status.equals("DONE", ignoreCase = true)) {

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onComplete
                ) {
                    Text("Mark Completed")
                }
            }
        }
    }
}

fun createTask(
    title: String,
    description: String,
    onResult: (Boolean, String) -> Unit
) {

    Thread {

        try {

            val url =
                URL("http://localhost:8080/api/tasks/create")

            val connection =
                url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.doOutput = true

            val json = JSONObject()

            json.put("title", title)
            json.put("description", description)
            json.put("user_id", 1)

            connection.outputStream.use { output ->

                output.write(
                    json.toString().toByteArray()
                )
            }

            val responseCode =
                connection.responseCode

            val responseText =

                if (responseCode in 200..299) {

                    connection.inputStream
                        .bufferedReader()
                        .use {
                            it.readText()
                        }

                } else {

                    connection.errorStream
                        ?.bufferedReader()
                        ?.use {
                            it.readText()
                        }
                        ?: "Unknown error"
                }

            Handler(
                Looper.getMainLooper()
            ).post {

                if (responseCode in 200..299) {

                    onResult(
                        true,
                        "Task created successfully"
                    )

                } else {

                    onResult(
                        false,
                        "Create task failed.\nHTTP $responseCode\n$responseText"
                    )
                }
            }

            connection.disconnect()

        } catch (e: Exception) {

            Handler(
                Looper.getMainLooper()
            ).post {

                onResult(
                    false,
                    "Unable to connect to server.\n${e.message}"
                )
            }
        }

    }.start()
}

fun loadTasks(
    onSuccess: (List<Task>) -> Unit,
    onError: (String) -> Unit
) {

    Thread {

        try {

            val url =
                URL("http://localhost:8080/api/tasks")

            val connection =
                url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode =
                connection.responseCode

            if (responseCode in 200..299) {

                val response =

                    connection.inputStream
                        .bufferedReader()
                        .use {
                            it.readText()
                        }

                val jsonArray =
                    JSONArray(response)

                val taskList =
                    mutableListOf<Task>()

                for (i in 0 until jsonArray.length()) {

                    val item =
                        jsonArray.getJSONObject(i)

                    taskList.add(
                        Task(
                            id = item.optInt("id"),
                            title = item.optString("title"),
                            description = item.optString("description"),
                            status = item.optString("status"),
                            userId = item.optInt("user_id")
                        )
                    )
                }

                Handler(
                    Looper.getMainLooper()
                ).post {

                    onSuccess(taskList)
                }

            } else {

                Handler(
                    Looper.getMainLooper()
                ).post {

                    onError(
                        "Failed to load tasks.\nHTTP $responseCode"
                    )
                }
            }

            connection.disconnect()

        } catch (e: Exception) {

            Handler(
                Looper.getMainLooper()
            ).post {

                onError(
                    "Unable to connect to server.\n${e.message}"
                )
            }
        }

    }.start()
}

fun updateTaskStatus(
    task: Task,
    newStatus: String,
    onResult: (Boolean, String) -> Unit
) {

    Thread {

        try {

            val url =
                URL("http://localhost:8080/api/tasks/update")

            val connection =
                url.openConnection() as HttpURLConnection

            connection.requestMethod = "PUT"

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.doOutput = true

            val json = JSONObject()

            json.put("id", task.id)
            json.put("title", task.title)
            json.put("description", task.description)
            json.put("status", newStatus)
            json.put("user_id", task.userId)

            connection.outputStream.use { output ->

                output.write(
                    json.toString().toByteArray()
                )
            }

            val responseCode =
                connection.responseCode

            val responseText =

                if (responseCode in 200..299) {

                    connection.inputStream
                        .bufferedReader()
                        .use {
                            it.readText()
                        }

                } else {

                    connection.errorStream
                        ?.bufferedReader()
                        ?.use {
                            it.readText()
                        }
                        ?: "Unknown error"
                }

            Handler(
                Looper.getMainLooper()
            ).post {

                if (responseCode in 200..299) {

                    onResult(
                        true,
                        "Task updated successfully"
                    )

                } else {

                    onResult(
                        false,
                        "Update task failed.\nHTTP $responseCode\n$responseText"
                    )
                }
            }

            connection.disconnect()

        } catch (e: Exception) {

            Handler(
                Looper.getMainLooper()
            ).post {

                onResult(
                    false,
                    "Unable to update task.\n${e.message}"
                )
            }
        }

    }.start()
}