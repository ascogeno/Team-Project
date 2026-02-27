package com.example.calendar.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventFile(
    val version: Int = 1,
    val events: List<CalendarEvent> = emptyList()
)

@Serializable
data class CalendarEvent(
    val name: String,
    val notes: String? = null,

    @SerialName("s_date") val sDate: String,
    @SerialName("s_time") val sTime: String,
    @SerialName("e_date") val eDate: String,
    @SerialName("e_time") val eTime: String,

    val color: String = "#0000ff",
    val recycled: Boolean = false
)