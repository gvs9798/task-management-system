package com.comp713.taskmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.comp713.taskmanagement.ui.theme.TaskManagementMobileTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

@Composable
fun TaskManagementScreen() {

    var result by remember {
        mutableStateOf("Press Load Tasks to retrieve tasks from the server.")
    }

    var loading by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Task Management",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Android Client",
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Button(
            enabled = !loading,
            onClick = {

                loading = true

                scope.launch {

                    result = loadTasks()

                    loading = false
                }
            }
        ) {
            Text("Load Tasks")
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 20.dp)
            )
        }

        Text(
            text = result,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

suspend fun loadTasks(): String {

    return withContext(Dispatchers.IO) {

        try {

            val url = URL("http://127.0.0.1:8080/api/tasks")

            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode

            if (responseCode == 200) {

                val response = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                if (response.trim() == "[]") {
                    "No tasks found."
                } else {
                    response
                }

            } else {

                "Server returned HTTP $responseCode"
            }

        } catch (e: Exception) {

            "Unable to connect to server.\n${e.message}"
        }
    }
}