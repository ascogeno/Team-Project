package com.example.calendar.data

import android.content.Context

class EventRepository(context: Context) {
    private val store = EventJsonStore(context)

    private val _events: MutableList<CalendarEvent> = store.load().toMutableList()
    val events: List<CalendarEvent> get() = _events

    fun add(event: CalendarEvent) {
        _events.add(event)
    }

    fun syncSave() {
        store.save(_events)
    }

    fun reloadFromDisk() {
        _events.clear()
        _events.addAll(store.load())
    }
}