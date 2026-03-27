package com.example.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.data.EventRepository

@Composable
fun SaveScreen() {
    val context = LocalContext.current
    val repo = remember { EventRepository(context) }
    var count by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Storage & Persistence",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        val tasks = repo.loadTasks()
                        count = tasks.size
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Check Saved Tasks")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Status: $count tasks on disk",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}