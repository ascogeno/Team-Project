package com.example.calendar.data

import kotlinx.serialization.Serializable

@Serializable
data class TaskFile(
    val version: Int = 1,
    val tasks: List<SavedCalendarTask> = emptyList()
)
@Serializable
data class SavedCalendarTask(
    val id: Long,
    val title: String,
    val notes: String = "",
    val startMinute: Int,
    val color: Long,
    val day: Int
)