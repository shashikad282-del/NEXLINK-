package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CalendarDate
import com.example.data.model.EventCategory
import com.example.ui.components.GlassCard
import com.example.ui.theme.ArcticCyan
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistSilver
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SlateMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditorSheet(
    request: EventEditorRequest,
    use24Hour: Boolean,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        existingId: String?,
        title: String,
        date: CalendarDate,
        startMinutes: Long,
        endMinutes: Long,
        allDay: Boolean,
        category: EventCategory,
        location: String,
        notes: String
    ) -> Unit,
    onDelete: (String) -> Unit
) {
    val existing = request.existingEvent
    var title by remember(request) { mutableStateOf(existing?.title.orEmpty()) }
    var date by remember(request) { mutableStateOf(request.initialDate) }
    var allDay by remember(request) { mutableStateOf(existing?.allDay ?: false) }
    var startMinutes by remember(request) { mutableLongStateOf(existing?.startMinutes ?: 540L) }
    var endMinutes by remember(request) { mutableLongStateOf(existing?.endMinutes ?: 600L) }
    var category by remember(request) {
        mutableStateOf(existing?.resolvedCategory() ?: EventCategory.WORK)
    }
    var location by remember(request) { mutableStateOf(existing?.location.orEmpty()) }
    var notes by remember(request) { mutableStateOf(existing?.notes.orEmpty()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val canSave = title.trim().isNotEmpty() && !isSaving

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = sheetState,
        containerColor = ObsidianBase,
        contentColor = IceWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (existing == null) "New Event" else "Edit Event",
                        style = MaterialTheme.typography.headlineSmall,
                        color = IceWhite
                    )
                    Text(
                        text = date.shortLabel(),
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcticCyan
                    )
                }

                if (existing != null) {
                    GlassCard(
                        cornerRadius = 12.dp,
                        accentBorderColor = DangerCoral.copy(alpha = 0.55f),
                        onClick = { if (!isSaving) showDeleteConfirm = true },
                        modifier = Modifier.testTag("delete_event_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete event",
                                tint = DangerCoral,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Delete",
                                style = MaterialTheme.typography.labelLarge,
                                color = DangerCoral
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 120) title = it },
                label = { Text("Event Title") },
                placeholder = { Text("e.g., Architecture Review") },
                singleLine = true,
                colors = glassFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_title_input")
            )

            // Date & All-day row
            GlassCard(
                cornerRadius = 18.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GlassCard(
                        cornerRadius = 12.dp,
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("event_date_picker_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Select date",
                                    tint = ArcticCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "${date.dayOfWeekFullName()}, ${date.shortLabel()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = IceWhite
                                )
                            }
                            Text(
                                text = "CHANGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = ArcticCyan
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "All-day event",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MistSilver
                        )
                        Switch(
                            checked = allDay,
                            onCheckedChange = { allDay = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianVoid,
                                checkedTrackColor = ArcticCyan,
                                uncheckedThumbColor = MistSilver,
                                uncheckedTrackColor = ObsidianElevated
                            ),
                            modifier = Modifier.testTag("event_all_day_switch")
                        )
                    }

                    if (!allDay) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlassCard(
                                cornerRadius = 12.dp,
                                onClick = { showStartTimePicker = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("event_start_time_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = "Start time",
                                        tint = ArcticCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "START",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SlateMuted
                                        )
                                        Text(
                                            text = CalendarDate.formatMinutes(
                                                startMinutes.toInt(),
                                                use24Hour
                                            ),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = IceWhite
                                        )
                                    }
                                }
                            }

                            GlassCard(
                                cornerRadius = 12.dp,
                                onClick = { showEndTimePicker = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("event_end_time_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = "End time",
                                        tint = ArcticCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "END",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SlateMuted
                                        )
                                        Text(
                                            text = CalendarDate.formatMinutes(
                                                endMinutes.toInt(),
                                                use24Hour
                                            ),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = IceWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "CATEGORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EventCategory.entries.forEach { cat ->
                        val isSelected = category == cat
                        GlassCard(
                            cornerRadius = 50.dp,
                            elevated = isSelected,
                            accentBorderColor = if (isSelected) cat.accent else null,
                            onClick = { category = cat },
                            modifier = Modifier.testTag("category_option_${cat.name.lowercase()}")
                        ) {
                            Text(
                                text = cat.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) cat.accent else MistSilver,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { if (it.length <= 120) location = it },
                label = { Text("Location (optional)") },
                singleLine = true,
                colors = glassFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_location_input")
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { if (it.length <= 500) notes = it },
                label = { Text("Notes (optional)") },
                minLines = 2,
                maxLines = 4,
                colors = glassFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("event_notes_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = { if (!isSaving) onDismiss() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MistSilver
                    )
                }

                Button(
                    onClick = {
                        onSave(
                            existing?.id,
                            title,
                            date,
                            startMinutes,
                            endMinutes,
                            allDay,
                            category,
                            location,
                            notes
                        )
                    },
                    enabled = canSave,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ArcticCyan,
                        contentColor = ObsidianVoid,
                        disabledContainerColor = ArcticCyan.copy(alpha = 0.35f),
                        disabledContentColor = ObsidianVoid
                    ),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(50.dp)
                        .testTag("save_event_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ObsidianVoid,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (existing == null) "Save Event" else "Update Event",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toEpochDay() * 86_400_000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        date = CalendarDate.fromEpochDay(millis / 86_400_000L)
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = ArcticCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MistSilver)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showStartTimePicker) {
        val startHour = (startMinutes / 60).toInt().coerceIn(0, 23)
        val startMin = (startMinutes % 60).toInt().coerceIn(0, 59)
        val timeState = rememberTimePickerState(
            initialHour = startHour,
            initialMinute = startMin,
            is24Hour = use24Hour
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            containerColor = ObsidianElevated,
            title = { Text("Start Time", color = IceWhite) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val newStart = (timeState.hour * 60 + timeState.minute).toLong()
                    startMinutes = newStart
                    if (endMinutes <= newStart) {
                        endMinutes = (newStart + 60L).coerceAtMost(1440L)
                    }
                    showStartTimePicker = false
                }) {
                    Text("Confirm", color = ArcticCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel", color = MistSilver)
                }
            }
        )
    }

    if (showEndTimePicker) {
        val clampedEnd = endMinutes.coerceAtMost(1439L)
        val endHour = (clampedEnd / 60).toInt().coerceIn(0, 23)
        val endMin = (clampedEnd % 60).toInt().coerceIn(0, 59)
        val timeState = rememberTimePickerState(
            initialHour = endHour,
            initialMinute = endMin,
            is24Hour = use24Hour
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            containerColor = ObsidianElevated,
            title = { Text("End Time", color = IceWhite) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val newEnd = (timeState.hour * 60 + timeState.minute).toLong()
                    endMinutes = newEnd.coerceAtLeast(startMinutes)
                    showEndTimePicker = false
                }) {
                    Text("Confirm", color = ArcticCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancel", color = MistSilver)
                }
            }
        )
    }

    if (showDeleteConfirm && existing != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = ObsidianElevated,
            title = { Text("Delete Event", color = IceWhite) },
            text = {
                Text(
                    text = "Remove \"${existing.title}\" from your schedule?",
                    color = MistSilver
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete(existing.id)
                }) {
                    Text("Delete", color = DangerCoral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Keep", color = MistSilver)
                }
            }
        )
    }
}

@Composable
private fun glassFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = IceWhite,
    unfocusedTextColor = IceWhite,
    focusedBorderColor = GlassBorderBright,
    unfocusedBorderColor = GlassBorderSubtle,
    focusedLabelColor = ArcticCyan,
    unfocusedLabelColor = SlateMuted,
    cursorColor = ArcticCyan,
    focusedPlaceholderColor = SlateMuted,
    unfocusedPlaceholderColor = SlateMuted
)
