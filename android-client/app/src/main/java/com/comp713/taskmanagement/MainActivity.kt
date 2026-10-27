package com.comp713.taskmanagement

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val status: String,
    val userId: Int
)

class MainActivity : ComponentActivity() {

    private val baseUrl = "http://localhost:8080"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        setContent {
            MaterialTheme {
                TaskManagementScreen()
            }
        }
    }

    @Composable
    fun TaskManagementScreen() {

        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }

        val notificationPermissionLauncher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { }

        LaunchedEffect(Unit) {
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Text(
                text = "Task Management",
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Android Client",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {
                        if (title.isBlank() || description.isBlank()) {
                            message =
                                "Please enter a title and description."
                        } else {
                            createTask(
                                title = title,
                                description = description
                            ) { result ->

                                message = result

                                if (result.startsWith("Task created")) {
                                    showNotification(
                                        "Task Created",
                                        "Task \"$title\" was created successfully."
                                    )

                                    title = ""
                                    description = ""

                                    loadTasks {
                                            loadedTasks ->
                                        tasks = loadedTasks
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Create Task")
                }

                Button(
                    onClick = {
                        loadTasks { loadedTasks ->

                            tasks = loadedTasks

                            message =
                                if (loadedTasks.isEmpty()) {
                                    "No tasks found."
                                } else {
                                    "Tasks loaded successfully."
                                }
                        }
                    }
                ) {
                    Text("Load Tasks")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (message.isNotEmpty()) {
                Text(message)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(tasks) { task ->

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = task.description
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Status: ${task.status}"
                            )

                            Text(
                                text = "Task ID: ${task.id}"
                            )

                            if (task.status != "DONE") {

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {

                                        updateTaskStatus(
                                            task = task,
                                            newStatus = "DONE"
                                        ) { result ->

                                            message = result

                                            if (
                                                result.startsWith(
                                                    "Task updated"
                                                )
                                            ) {

                                                showNotification(
                                                    "Task Completed",
                                                    "Task \"${task.title}\" is now DONE."
                                                )

                                                loadTasks {
                                                        loadedTasks ->
                                                    tasks = loadedTasks
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text("Mark Completed")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createTask(
        title: String,
        description: String,
        callback: (String) -> Unit
    ) {

        Thread {

            try {

                val url =
                    URL("$baseUrl/api/tasks/create")

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

                connection.outputStream.use {
                        output ->
                    output.write(
                        json.toString().toByteArray()
                    )
                }

                val responseCode =
                    connection.responseCode

                val result =
                    if (
                        responseCode in 200..299
                    ) {
                        "Task created successfully."
                    } else {
                        "Failed to create task. HTTP $responseCode"
                    }

                Handler(
                    Looper.getMainLooper()
                ).post {
                    callback(result)
                }

                connection.disconnect()

            } catch (e: Exception) {

                Handler(
                    Looper.getMainLooper()
                ).post {
                    callback(
                        "Unable to connect to server."
                    )
                }
            }
        }.start()
    }

    private fun loadTasks(
        callback: (List<Task>) -> Unit
    ) {

        Thread {

            try {

                val url =
                    URL("$baseUrl/api/tasks")

                val connection =
                    url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode =
                    connection.responseCode

                if (
                    responseCode in 200..299
                ) {

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

                    for (
                    i in 0 until jsonArray.length()
                    ) {

                        val obj =
                            jsonArray.getJSONObject(i)

                        taskList.add(
                            Task(
                                id =
                                    obj.getInt("id"),
                                title =
                                    obj.getString("title"),
                                description =
                                    obj.optString(
                                        "description",
                                        ""
                                    ),
                                status =
                                    obj.getString("status"),
                                userId =
                                    obj.getInt("user_id")
                            )
                        )
                    }

                    Handler(
                        Looper.getMainLooper()
                    ).post {
                        callback(taskList)
                    }

                } else {

                    Handler(
                        Looper.getMainLooper()
                    ).post {
                        callback(emptyList())
                    }
                }

                connection.disconnect()

            } catch (e: Exception) {

                Handler(
                    Looper.getMainLooper()
                ).post {
                    callback(emptyList())
                }
            }
        }.start()
    }

    private fun updateTaskStatus(
        task: Task,
        newStatus: String,
        callback: (String) -> Unit
    ) {

        Thread {

            try {

                val url =
                    URL("$baseUrl/api/tasks/update")

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

                val json =
                    JSONObject()

                json.put(
                    "id",
                    task.id
                )

                json.put(
                    "title",
                    task.title
                )

                json.put(
                    "description",
                    task.description
                )

                json.put(
                    "status",
                    newStatus
                )

                json.put(
                    "user_id",
                    task.userId
                )

                connection.outputStream.use {
                        output ->
                    output.write(
                        json.toString().toByteArray()
                    )
                }

                val responseCode =
                    connection.responseCode

                val result =
                    if (
                        responseCode in 200..299
                    ) {
                        "Task updated successfully."
                    } else {
                        "Failed to update task. HTTP $responseCode"
                    }

                Handler(
                    Looper.getMainLooper()
                ).post {
                    callback(result)
                }

                connection.disconnect()

            } catch (e: Exception) {

                Handler(
                    Looper.getMainLooper()
                ).post {
                    callback(
                        "Unable to connect to server."
                    )
                }
            }
        }.start()
    }

    private fun createNotificationChannel() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    "task_channel",
                    "Task Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )

            channel.description =
                "Notifications for task updates"

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }

    private fun showNotification(
        title: String,
        message: String
    ) {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                android.app.Notification.Builder(
                    this,
                    "task_channel"
                )
                    .setSmallIcon(
                        android.R.drawable.ic_dialog_info
                    )
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .build()

            } else {

                android.app.Notification.Builder(
                    this
                )
                    .setSmallIcon(
                        android.R.drawable.ic_dialog_info
                    )
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .build()
            }

        val manager =
            getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        manager.notify(
            System.currentTimeMillis()
                .toInt(),
            notification
        )
    }
}