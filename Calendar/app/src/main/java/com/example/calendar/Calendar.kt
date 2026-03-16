package com.example.calendar

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import kotlinx.coroutines.delay

/**
 * Data Model for a Calendar Event.
 * All time calculations are based on total minutes from midnight (0 to 1439).
 */
data class CalendarTask(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    val title: String,
    val notes: String = "",
    val startMinute: Int, // Start time represented as minutes from 00:00
    val endMinute: Int,   // End time represented as minutes from 00:00
    val color: Color,
    val day: Int          // The day of the month this task belongs to
)

@Composable
fun CalendarScreen() {
    // --- Calendar Initialization ---
    val calendar = remember { Calendar.getInstance() }
    val todayDay = remember { calendar.get(Calendar.DAY_OF_MONTH) }
    val daysInMonth = remember { calendar.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val currentMonthName = remember {
        SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time)
    }

    // --- State Management ---
    // Tracks which day is currently active in the view
    var selectedDay by remember { mutableIntStateOf(todayDay) }

    // An observable list of tasks. Using mutableStateListOf allows Compose to
    // automatically recompose when items are added, removed, or updated.
    val tasks = remember { mutableStateListOf<CalendarTask>() }

    // Tracks current time to position the "Red Line" indicator
    var currentMinutesOfDay by remember {
        mutableIntStateOf(Calendar.getInstance().run { get(Calendar.HOUR_OF_DAY) * 60 + get(Calendar.MINUTE) })
    }

    // Dialog and UI interaction states
    var showDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<CalendarTask?>(null) }
    var clickedHour by remember { mutableIntStateOf(9) }

    // Drag and Drop Logic States
    var draggingTaskId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    // Scroll States for the Date Row (Horizontal) and Time Grid (Vertical)
    val dateListState = rememberLazyListState()
    val verticalScrollState = rememberScrollState()

    // Standard height for one hour in the grid
    val hourHeight = 90.dp
    val density = LocalDensity.current

    // 1. EFFECT: Background Timer
    // LaunchedEffect(Unit) runs once when the Composable enters the Composition.
    // This loop updates the 'currentMinutesOfDay' state every 60 seconds.
    LaunchedEffect(Unit) {
        while (true) {
            val cal = Calendar.getInstance()
            currentMinutesOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            delay(60_000)
        }
    }

    // 2. EFFECT: Initial View Positioning
    // Automatically scrolls the UI to the current date and time upon opening.
    LaunchedEffect(Unit) {
        // Center/Scroll the horizontal date row to today
        dateListState.scrollToItem((todayDay - 1).coerceAtLeast(0))

        // Calculate vertical scroll position in pixels.
        // We scroll to (Current Hour - 2) to provide visual context of preceding time.
        val currentHour = currentMinutesOfDay / 60
        val scrollTargetHour = (currentHour - 2).coerceAtLeast(0)
        val pxToScroll = with(density) { (scrollTargetHour * hourHeight.value.dp.toPx()).toInt() }
        verticalScrollState.scrollTo(pxToScroll)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingTask = null; clickedHour = 9; showDialog = true },
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.border(1.dp, Color.Black, CircleShape)
            ) { Icon(Icons.Default.Add, contentDescription = "Add Task") }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // Month Title
            Text(
                text = currentMonthName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)
            )

            // Horizontal Date Row (Selector)
            LazyRow(
                state = dateListState,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(daysInMonth) { index ->
                    val day = index + 1
                    DateItem(
                        day = day,
                        isSelected = day == selectedDay,
                        isToday = day == todayDay,
                        onDateClick = { selectedDay = day }
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            // Main Content: 24-Hour Grid and Events
            Box(modifier = Modifier.fillMaxSize().verticalScroll(verticalScrollState)) {

                // LAYER 1: Background Hour Grid Lines
                Column {
                    repeat(24) { hour ->
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(hourHeight)
                            .clickable {
                                editingTask = null
                                clickedHour = hour
                                showDialog = true
                            }
                        ) {
                            HourLabel(hour = hour)
                        }
                    }
                }

                // LAYER 2: Current Time Indicator (Red Line)
                // Only rendered if the user is looking at the current day.
                if (selectedDay == todayDay) {
                    // Offset is calculated by converting (minutes/60) to a fraction of hourHeight
                    val timeLineYOffset = (currentMinutesOfDay / 60f) * hourHeight.value
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = timeLineYOffset.dp)
                            .zIndex(5f) // Ensures line is drawn above background but potentially below dialogs
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .padding(start = 65.dp) // Starts after the hour label text
                                .background(Color.Red)
                        )
                    }
                }

                // LAYER 3: Tasks/Events
                // Filter the global task list for the selected day and iterate.
                tasks.filter { it.day == selectedDay }.forEach { task ->
                    val isDragging = draggingTaskId == task.id

                    // Positioning Math:
                    // Start position = (StartMinute / 60) * height per hour
                    val startOffset = (task.startMinute / 60f) * hourHeight.value
                    // Height = (DurationMinutes / 60) * height per hour
                    val duration = task.endMinute - task.startMinute
                    val taskHeight = (duration / 60f) * hourHeight.value

                    EventCard(
                        task = task,
                        modifier = Modifier
                            .padding(start = 70.dp, end = 16.dp)
                            .zIndex(if (isDragging) 3f else 1f) // Elevate card during drag
                            .offset(y = startOffset.dp + (if (isDragging) dragOffsetY.dp else 0.dp))
                            .height(taskHeight.dp)
                            .pointerInput(task) {
                                // Handles drag-to-reschedule logic
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { draggingTaskId = task.id },
                                    onDragEnd = {
                                        // Calculate the change in minutes based on pixel drag distance
                                        val minutesMoved = (dragOffsetY / hourHeight.toPx()) * 60
                                        val newUnsnappedStart = task.startMinute + minutesMoved.toInt()

                                        // Snap logic: Rounds the new start time to the nearest 15-minute increment
                                        val snappedStart = ((newUnsnappedStart + 7) / 15 * 15).coerceIn(0, 1440 - duration)

                                        val index = tasks.indexOfFirst { it.id == task.id }
                                        if (index != -1) {
                                            // Update the specific task instance in the state list
                                            tasks[index] = tasks[index].copy(
                                                startMinute = snappedStart,
                                                endMinute = snappedStart + duration
                                            )
                                        }
                                        // Reset drag state
                                        draggingTaskId = null
                                        dragOffsetY = 0f
                                    },
                                    onDragCancel = {
                                        draggingTaskId = null
                                        dragOffsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        // Consume the touch event so parent scroll doesn't intercept
                                        change.consume()
                                        dragOffsetY += dragAmount.y
                                    }
                                )
                            },
                        onClick = { editingTask = task }
                    )
                }
            }
        }

        // --- Dialog Logic ---
        // Handles both the creation of new tasks and editing of existing ones.
        if (showDialog || editingTask != null) {
            AddTaskDialog(
                initialHour = clickedHour,
                existingTask = editingTask,
                onDismiss = { showDialog = false; editingTask = null },
                onTaskAdded = { newTask ->
                    if (editingTask != null) {
                        // Edit Mode: Replace item in the list
                        val index = tasks.indexOfFirst { it.id == editingTask!!.id }
                        if (index != -1) tasks[index] = newTask
                    } else {
                        // Add Mode: Insert new item
                        tasks.add(newTask.copy(day = selectedDay))
                    }
                    showDialog = false
                    editingTask = null
                },
                onDeleteTask = { taskToDelete ->
                    tasks.removeIf { it.id == taskToDelete.id }
                    showDialog = false
                    editingTask = null
                }
            )
        }
    }
}

/**
 * Renders the hour labels (e.g., "12 PM") and the horizontal grid lines.
 */
@Composable
fun HourLabel(hour: Int) {
    val label = when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }
    // alpha = 0.2f provides a subtle, non-intrusive grid line
    Box(modifier = Modifier.fillMaxSize().border(0.5.dp, Color.LightGray.copy(alpha = 0.2f))) {
        Text(
            text = label,
            modifier = Modifier.padding(start = 12.dp, top = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

/**
 * Visual representation of an event.
 */
@Composable
fun EventCard(task: CalendarTask, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        color = task.color,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if (task.notes.isNotBlank()) {
                Text(
                    text = task.notes,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Popup dialog for creating/editing tasks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    initialHour: Int,
    existingTask: CalendarTask? = null,
    onDismiss: () -> Unit,
    onTaskAdded: (CalendarTask) -> Unit,
    onDeleteTask: (CalendarTask) -> Unit
) {
    // Local state for form fields
    var taskName by remember { mutableStateOf(existingTask?.title ?: "") }
    var taskNotes by remember { mutableStateOf(existingTask?.notes ?: "") }
    var pickingStart by remember { mutableStateOf(true) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Material3 TimePicker states
    val startState = rememberTimePickerState(
        initialHour = existingTask?.let { it.startMinute / 60 } ?: initialHour,
        initialMinute = existingTask?.let { it.startMinute % 60 } ?: 0
    )
    val endState = rememberTimePickerState(
        initialHour = existingTask?.let { it.endMinute / 60 } ?: (initialHour + 1),
        initialMinute = existingTask?.let { it.endMinute % 60 } ?: 0
    )

    val colors = listOf(Color(0xFFFFD7D7), Color(0xFFE3F2FD), Color(0xFFF1F8E9), Color(0xFFFFF9C4), Color(0xFFF3E5F5))
    var selectedColor by remember { mutableStateOf(existingTask?.color ?: colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text(if (existingTask == null) "New Event" else "Edit Event")
                if (existingTask != null) {
                    IconButton(onClick = { onDeleteTask(existingTask) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = taskName, onValueChange = { taskName = it }, label = { Text("Event Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = taskNotes, onValueChange = { taskNotes = it }, label = { Text("Details") }, modifier = Modifier.fillMaxWidth())

                // Time Selection Buttons
                Row(modifier = Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { pickingStart = true; showTimePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(formatTime(startState.hour, startState.minute))
                    }
                    OutlinedButton(onClick = { pickingStart = false; showTimePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(formatTime(endState.hour, endState.minute))
                    }
                }

                // Color Selection Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { color ->
                        Box(modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(if (selectedColor == color) 2.dp else 0.dp, Color.Black, CircleShape)
                            .clickable { selectedColor = color })
                    }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            // Conversion logic from TimePicker state to minute integer
            val startMin = startState.hour * 60 + startState.minute
            val endMin = endState.hour * 60 + endState.minute
            Button(
                onClick = {
                    onTaskAdded(CalendarTask(
                        id = existingTask?.id ?: System.currentTimeMillis(),
                        title = taskName,
                        notes = taskNotes,
                        startMinute = startMin,
                        endMinute = endMin,
                        color = selectedColor,
                        day = existingTask?.day ?: 0
                    ))
                },
                // Prevents creating tasks with zero/negative duration or no title
                enabled = taskName.isNotBlank() && endMin > startMin
            ) { Text("Save") }
        }
    )

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = { TextButton(onClick = { showTimePicker = false }) { Text("OK") } }
        ) {
            TimePicker(state = if (pickingStart) startState else endState)
        }
    }
}

/**
 * Helper to convert 24h format to 12h AM/PM string.
 */
fun formatTime(h: Int, m: Int): String {
    val displayH = if (h % 12 == 0) 12 else h % 12
    val amPm = if (h < 12) "AM" else "PM"
    return "$displayH:${m.toString().padStart(2, '0')} $amPm"
}

/**
 * Standard Material TimePickerDialog implementation.
 */
@Composable
fun TimePickerDialog(onDismissRequest: () -> Unit, confirmButton: @Composable () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp, modifier = Modifier.padding(24.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                content()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { confirmButton() }
            }
        }
    }
}

/**
 * Individual date circle/selector in the horizontal list.
 */
@Composable
fun DateItem(day: Int, isSelected: Boolean, isToday: Boolean, onDateClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .width(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFEEEEEE) else Color.Transparent)
            // Today's date is highlighted with a blue border
            .border(width = if (isToday) 2.dp else 0.dp, color = if (isToday) Color(0xFF2196F3) else Color.Transparent, shape = RoundedCornerShape(12.dp))
            .clickable { onDateClick() }
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = day.toString(),
            fontSize = 18.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
        )
        // Red dot/line to indicate selection
        if (isSelected) Box(modifier = Modifier.padding(top = 4.dp).height(3.dp).width(25.dp).background(Color.Red))
    }
}