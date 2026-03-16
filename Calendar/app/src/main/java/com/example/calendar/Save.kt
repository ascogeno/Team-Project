package com.example.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.data.CalendarEvent
import com.example.calendar.data.EventRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveScreen() {
    // We use a Column here because the Scaffold is already provided by MainActivity
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


        // Persistence Logic
        val context = LocalContext.current
        val repo = remember { EventRepository(context) }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        repo.add(
                            CalendarEvent(
                                name = "Lunch",
                                notes = "Eating with Patricia",
                                sDate = "2026-03-16",
                                sTime = "12:00",
                                eDate = "2026-03-16",
                                eTime = "13:00",
                                color = "#0000ff",
                                recycled = false
                            )
                        )
                        repo.syncSave()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test Save Event")
                }

                Button(
                    onClick = { repo.reloadFromDisk() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Test Load From Disk")
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Status: ${repo.events.size} events in memory",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}