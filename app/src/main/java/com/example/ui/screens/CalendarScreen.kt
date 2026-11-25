package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CalendarDate
import com.example.data.model.CalendarEvent
import com.example.data.repository.CalendarPreferences
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassEventCard
import com.example.ui.theme.ArcticCyan
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.GlassBorderBright
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassSurfaceHigh
import com.example.ui.theme.GlassSurfaceLow
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistSilver
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SlateMuted

private data class MonthGridCell(
    val date: CalendarDate,
    val inCurrentMonth: Boolean
)

@Composable
fun CalendarScreen(
    eventsState: UiState<List<CalendarEvent>>,
    selectedDate: CalendarDate,
    displayedMonth: CalendarDate,
    preferences: CalendarPreferences,
    onSelectDate: (CalendarDate) -> Unit,
    onStepMonth: (Int) -> Unit,
    onJumpToToday: () -> Unit,
    onAddEvent: (CalendarDate) -> Unit,
    onEditEvent: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val allEvents = (eventsState as? UiState.Success)?.data.orEmpty()
    val eventsByDay = remember(allEvents) { allEvents.groupBy { it.dateEpochDay } }
    val selectedDayEvents = remember(eventsByDay, selectedDate) {
        eventsByDay[selectedDate.toEpochDay()].orEmpty()
    }
    val today = remember { CalendarDate.today() }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideLayout = maxWidth >= 720.dp

        if (isWideLayout) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                ) {
                    CalendarHeader(
                        displayedMonth = displayedMonth,
                        isShowingTodayMonth = displayedMonth.year == today.year &&
                            displayedMonth.month == today.month &&
                            selectedDate == today,
                        onStepMonth = onStepMonth,
                        onJumpToToday = onJumpToToday
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    MonthGlassGrid(
                        displayedMonth = displayedMonth,
                        selectedDate = selectedDate,
                        today = today,
                        weekStartsOnSunday = preferences.weekStartsOnSunday,
                        eventsByDay = eventsByDay,
                        onSelectDate = onSelectDate
                    )
                }

                DayAgendaColumn(
                    selectedDate = selectedDate,
                    today = today,
                    eventsState = eventsState,
                    dayEvents = selectedDayEvents,
                    use24Hour = preferences.use24HourFormat,
                    onAddEvent = { onAddEvent(selectedDate) },
                    onEditEvent = onEditEvent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 104.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CalendarHeader(
                        displayedMonth = displayedMonth,
                        isShowingTodayMonth = displayedMonth.year == today.year &&
                            displayedMonth.month == today.month &&
                            selectedDate == today,
                        onStepMonth = onStepMonth,
                        onJumpToToday = onJumpToToday
                    )
                }

                item {
                    MonthGlassGrid(
                        displayedMonth = displayedMonth,
                        selectedDate = selectedDate,
                        today = today,
                        weekStartsOnSunday = preferences.weekStartsOnSunday,
                        eventsByDay = eventsByDay,
                        onSelectDate = onSelectDate
                    )
                }

                item {
                    DaySectionHeader(
                        selectedDate = selectedDate,
                        today = today,
                        eventCount = selectedDayEvents.size,
                        onAddEvent = { onAddEvent(selectedDate) }
                    )
                }

                when (eventsState) {
                    is UiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = ArcticCyan,
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }
                    }
                    is UiState.Error -> {
                        item {
                            GlassCard(
                                cornerRadius = 18.dp,
                                accentBorderColor = DangerCoral,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = eventsState.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DangerCoral,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                    is UiState.Success -> {
                        if (selectedDayEvents.isEmpty()) {
                            item {
                                EmptyDayGlassCard(
                                    selectedDate = selectedDate,
                                    onAddEvent = { onAddEvent(selectedDate) }
                                )
                            }
                        } else {
                            items(
                                items = selectedDayEvents,
                                key = { it.id }
                            ) { event ->
                                GlassEventCard(
                                    event = event,
                                    use24Hour = preferences.use24HourFormat,
                                    onClick = { onEditEvent(event) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayAgendaColumn(
    selectedDate: CalendarDate,
    today: CalendarDate,
    eventsState: UiState<List<CalendarEvent>>,
    dayEvents: List<CalendarEvent>,
    use24Hour: Boolean,
    onAddEvent: () -> Unit,
    onEditEvent: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DaySectionHeader(
                selectedDate = selectedDate,
                today = today,
                eventCount = dayEvents.size,
                onAddEvent = onAddEvent
            )
        }
        when (eventsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ArcticCyan, strokeWidth = 2.5.dp)
                    }
                }
            }
            is UiState.Error -> {
                item {
                    GlassCard(
                        cornerRadius = 18.dp,
                        accentBorderColor = DangerCoral,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = eventsState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DangerCoral,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            is UiState.Success -> {
                if (dayEvents.isEmpty()) {
                    item {
                        EmptyDayGlassCard(
                            selectedDate = selectedDate,
                            onAddEvent = onAddEvent
                        )
                    }
                } else {
                    items(items = dayEvents, key = { it.id }) { event ->
                        GlassEventCard(
                            event = event,
                            use24Hour = use24Hour,
                            onClick = { onEditEvent(event) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarHeader(
    displayedMonth: CalendarDate,
    isShowingTodayMonth: Boolean,
    onStepMonth: (Int) -> Unit,
    onJumpToToday: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = CalendarDate.monthName(displayedMonth.month),
                style = MaterialTheme.typography.displaySmall,
                color = IceWhite
            )
            Text(
                text = displayedMonth.year.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = ArcticCyan,
                letterSpacing = 2.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isShowingTodayMonth) {
                GlassCard(
                    cornerRadius = 50.dp,
                    accentBorderColor = ArcticCyan.copy(alpha = 0.5f),
                    onClick = onJumpToToday,
                    modifier = Modifier.testTag("jump_today_button")
                ) {
                    Text(
                        text = stringResource(R.string.action_today),
                        style = MaterialTheme.typography.labelMedium,
                        color = ArcticCyan,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            GlassCard(cornerRadius = 16.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onStepMonth(-1) },
                        modifier = Modifier.testTag("previous_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = "Previous month",
                            tint = IceWhite
                        )
                    }
                    IconButton(
                        onClick = { onStepMonth(1) },
                        modifier = Modifier.testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = "Next month",
                            tint = IceWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthGlassGrid(
    displayedMonth: CalendarDate,
    selectedDate: CalendarDate,
    today: CalendarDate,
    weekStartsOnSunday: Boolean,
    eventsByDay: Map<Long, List<CalendarEvent>>,
    onSelectDate: (CalendarDate) -> Unit
) {
    val weekdayHeaders = remember(weekStartsOnSunday) {
        if (weekStartsOnSunday) {
            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
        } else {
            listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        }
    }

    val cells = remember(displayedMonth, weekStartsOnSunday) {
        buildMonthGridCells(displayedMonth, weekStartsOnSunday)
    }

    GlassCard(
        cornerRadius = 24.dp,
        elevated = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("month_glass_grid")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdayHeaders.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            cells.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { cell ->
                        val dayEvents = eventsByDay[cell.date.toEpochDay()].orEmpty()
                        CalendarDayCell(
                            cell = cell,
                            isSelected = cell.date == selectedDate,
                            isToday = cell.date == today,
                            dayEvents = dayEvents,
                            onClick = { onSelectDate(cell.date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    cell: MonthGridCell,
    isSelected: Boolean,
    isToday: Boolean,
    dayEvents: List<CalendarEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }

    val bgModifier = when {
        isSelected -> Modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    ArcticCyan.copy(alpha = 0.28f),
                    ArcticCyan.copy(alpha = 0.12f)
                )
            )
        )
        isToday -> Modifier.background(GlassSurfaceHigh)
        cell.inCurrentMonth -> Modifier.background(GlassSurfaceLow.copy(alpha = 0.4f))
        else -> Modifier.background(Color.Transparent)
    }

    val borderModifier = when {
        isSelected -> Modifier.border(1.2.dp, ArcticCyan.copy(alpha = 0.85f), shape)
        isToday -> Modifier.border(1.dp, GlassBorderBright, shape)
        cell.inCurrentMonth -> Modifier.border(0.5.dp, GlassBorderSubtle, shape)
        else -> Modifier
    }

    val textColor = when {
        isSelected -> IceWhite
        isToday -> ArcticCyan
        cell.inCurrentMonth -> IceWhite.copy(alpha = 0.9f)
        else -> SlateMuted.copy(alpha = 0.45f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .minimumInteractiveComponentSize()
            .clip(shape)
            .then(bgModifier)
            .then(borderModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = ArcticCyan),
                onClick = onClick
            )
            .testTag("calendar_day_${cell.date.year}_${cell.date.month}_${cell.date.day}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = cell.date.day.toString(),
                fontFamily = OutfitFontFamily,
                fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 15.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(3.dp))
            if (dayEvents.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dayEvents.take(3).forEach { evt ->
                        Box(
                            modifier = Modifier
                                .size(4.5.dp)
                                .clip(CircleShape)
                                .background(evt.resolvedCategory().accent)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.5.dp))
            }
        }
    }
}

@Composable
private fun DaySectionHeader(
    selectedDate: CalendarDate,
    today: CalendarDate,
    eventCount: Int,
    onAddEvent: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val prefix = when (selectedDate.toEpochDay() - today.toEpochDay()) {
                0L -> "Today · "
                1L -> "Tomorrow · "
                -1L -> "Yesterday · "
                else -> ""
            }
            Text(
                text = "$prefix${selectedDate.dayOfWeekFullName()}",
                style = MaterialTheme.typography.titleLarge,
                color = IceWhite
            )
            Text(
                text = "${selectedDate.shortLabel()} · ${
                    when (eventCount) {
                        0 -> "Clear schedule"
                        1 -> "1 event"
                        else -> "$eventCount events"
                    }
                }",
                style = MaterialTheme.typography.labelMedium,
                color = MistSilver
            )
        }

        GlassCard(
            cornerRadius = 14.dp,
            accentBorderColor = ArcticCyan.copy(alpha = 0.5f),
            onClick = onAddEvent,
            modifier = Modifier.testTag("day_add_event_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add event",
                    tint = ArcticCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Add",
                    style = MaterialTheme.typography.labelLarge,
                    color = ArcticCyan
                )
            }
        }
    }
}

@Composable
private fun EmptyDayGlassCard(
    selectedDate: CalendarDate,
    onAddEvent: () -> Unit
) {
    GlassCard(
        cornerRadius = 22.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("empty_day_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarToday,
                contentDescription = null,
                tint = SlateMuted,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No events scheduled",
                style = MaterialTheme.typography.titleMedium,
                color = IceWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap below to schedule an event for ${selectedDate.shortLabel()}.",
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddEvent,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ArcticCyan,
                    contentColor = ObsidianVoid
                ),
                modifier = Modifier.testTag("empty_day_create_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.action_add_event),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun buildMonthGridCells(
    monthFirstDay: CalendarDate,
    weekStartsOnSunday: Boolean
): List<MonthGridCell> {
    val firstDay = monthFirstDay.firstOfMonth()
    // Monday=0 .. Sunday=6
    val firstDowMonZero = firstDay.dayOfWeekMondayZero()
    val leadingDays = if (weekStartsOnSunday) {
        (firstDowMonZero + 1) % 7
    } else {
        firstDowMonZero
    }

    val daysInThisMonth = CalendarDate.daysInMonth(firstDay.year, firstDay.month)
    val totalRaw = leadingDays + daysInThisMonth
    val totalCells = if (totalRaw <= 35) 35 else 42
    val gridStart = firstDay.plusDays(-leadingDays)

    return List(totalCells) { offset ->
        val date = gridStart.plusDays(offset)
        MonthGridCell(
            date = date,
            inCurrentMonth = date.year == firstDay.year && date.month == firstDay.month
        )
    }
}
