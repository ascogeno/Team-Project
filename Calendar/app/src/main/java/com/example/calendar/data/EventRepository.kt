package com.example.calendar.data

import android.content.Context
import com.example.calendar.CalendarTask

class EventRepository(context: Context) {
    private val store = EventJsonStore(context)

    fun loadTasks(): List<CalendarTask> = store.loadTasks()

    fun saveTasks(tasks: List<CalendarTask>) {
        store.saveTasks(tasks)
    }
}