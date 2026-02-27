package com.example.calendar.data

import android.content.Context
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class EventJsonStore(
    private val context: Context,
    private val fileName: String = "events.json"
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val file: File get() = File(context.filesDir, fileName)

    fun load(): List<CalendarEvent> {
        if (!file.exists()) return emptyList()

        return runCatching {
            json.decodeFromString<EventFile>(file.readText()).events
        }.getOrElse {
            emptyList()
        }
    }

    fun save(events: List<CalendarEvent>) {
        val payload = EventFile(version = 1, events = events)

        val tmp = File(context.filesDir, "$fileName.tmp")
        tmp.writeText(json.encodeToString(payload))

        if (file.exists()) file.delete()
        if (!tmp.renameTo(file)) {
            tmp.copyTo(file, overwrite = true)
            tmp.delete()
        }
    }
}