package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.CalendarDate
import com.example.data.model.CalendarEvent
import com.example.data.model.EventCategory
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassEventCard
import com.example.ui.theme.ArcticCyan
import com.example.ui.theme.DangerCoral
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistSilver
import com.example.ui.theme.SlateMuted

@Composable
fun AgendaScreen(
    eventsState: UiState<List<CalendarEvent>>,
    searchQuery: String,
    selectedCategory: EventCategory?,
    use24Hour: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (EventCategory?) -> Unit,
    onEditEvent: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayEpoch = remember { CalendarDate.today().toEpochDay() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Agenda",
                style = MaterialTheme.typography.displaySmall,
                color = IceWhite
            )
            Text(
                text = "CHRONOLOGICAL TIMELINE",
                style = MaterialTheme.typography.labelSmall,
                color = ArcticCyan
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Glass search bar
            GlassCard(
                cornerRadius = 18.dp,
                elevated = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search events",
                        tint = SlateMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search title, location, or notes...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateMuted
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = IceWhite),
                            cursorBrush = SolidColor(ArcticCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("agenda_search_input")
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear search",
                                tint = MistSilver,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryFilterChip(
                    label = "All",
                    accent = ArcticCyan,
                    selected = selectedCategory == null,
                    onClick = { onSelectCategory(null) }
                )
                EventCategory.entries.forEach { category ->
                    CategoryFilterChip(
                        label = category.label,
                        accent = category.accent,
                        selected = selectedCategory == category,
                        onClick = {
                            onSelectCategory(if (selectedCategory == category) null else category)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (eventsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ArcticCyan, strokeWidth = 2.5.dp)
                    }
                }
                is UiState.Error -> {
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
                is UiState.Success -> {
                    val filtered = remember(eventsState.data, searchQuery, selectedCategory) {
                        val q = searchQuery.trim().lowercase()
                        eventsState.data.filter { event ->
                            val matchesCategory = selectedCategory == null ||
                                event.resolvedCategory() == selectedCategory
                            val matchesQuery = q.isEmpty() ||
                                event.title.lowercase().contains(q) ||
                                event.location.lowercase().contains(q) ||
                                event.notes.lowercase().contains(q)
                            matchesCategory && matchesQuery
                        }
                    }

                    val upcoming = remember(filtered, todayEpoch) {
                        filtered.filter { it.dateEpochDay >= todayEpoch }
                    }
                    val past = remember(filtered, todayEpoch) {
                        filtered.filter { it.dateEpochDay < todayEpoch }.reversed()
                    }

                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            GlassCard(
                                cornerRadius = 24.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("agenda_empty_card")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.EventNote,
                                        contentDescription = null,
                                        tint = SlateMuted,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = if (eventsState.data.isEmpty()) {
                                            "Your timeline is clear"
                                        } else {
                                            "No matching events"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = IceWhite
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (eventsState.data.isEmpty()) {
                                            "Create an event from the + button to populate your schedule."
                                        } else {
                                            "Try clearing your search or category filter."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateMuted,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 104.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (upcoming.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "UPCOMING · ${upcoming.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ArcticCyan,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(items = upcoming, key = { "up_${it.id}" }) { event ->
                                    GlassEventCard(
                                        event = event,
                                        use24Hour = use24Hour,
                                        showDateBadge = true,
                                        onClick = { onEditEvent(event) }
                                    )
                                }
                            }

                            if (past.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "EARLIER · ${past.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateMuted,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                                items(items = past, key = { "past_${it.id}" }) { event ->
                                    GlassEventCard(
                                        event = event,
                                        use24Hour = use24Hour,
                                        showDateBadge = true,
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
}

@Composable
private fun CategoryFilterChip(
    label: String,
    accent: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        cornerRadius = 50.dp,
        elevated = selected,
        accentBorderColor = if (selected) accent else null,
        onClick = onClick,
        modifier = Modifier.testTag("filter_chip_${label.lowercase()}")
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accent else MistSilver,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
        )
    }
}
