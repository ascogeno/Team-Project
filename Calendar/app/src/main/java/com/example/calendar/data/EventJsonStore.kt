package com.example.calendar.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.calendar.CalendarTask
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

    fun loadTasks(): List<CalendarTask> {
        if (!file.exists()) return emptyList()

        return runCatching {
            val parsed = json.decodeFromString<TaskFile>(file.readText())
            parsed.tasks.map { saved ->
                CalendarTask(
                    id = saved.id,
                    title = saved.title,
                    notes = saved.notes,
                    startMinute = saved.startMinute,
                    endMinute = saved.endMinute,
                    color = Color(saved.color),
                    day = saved.day
                )
            }
        }.getOrElse {
            emptyList()
        }
    }

    fun saveTasks(tasks: List<CalendarTask>) {
        val payload = TaskFile(
            version = 1,
            tasks = tasks.map { task ->
                SavedCalendarTask(
                    id = task.id,
                    title = task.title,
                    notes = task.notes,
                    startMinute = task.startMinute,
                    endMinute = task.endMinute,
                    color = task.color.value.toLong(),
                    day = task.day
                )
            }
        )

        val tmp = File(context.filesDir, "$fileName.tmp")
        tmp.writeText(json.encodeToString(payload))

        if (file.exists()) file.delete()
        if (!tmp.renameTo(file)) {
            tmp.copyTo(file, overwrite = true)
            tmp.delete()
        }
    }
}