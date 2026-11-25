package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CalendarDate
import com.example.data.model.CalendarEvent
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistSilver
import com.example.ui.theme.SlateMuted

@Composable
fun GlassEventCard(
    event: CalendarEvent,
    use24Hour: Boolean,
    showDateBadge: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = event.resolvedCategory()
    val timeLabel = CalendarDate.formatTimeRange(
        startMinutes = event.startMinutes,
        endMinutes = event.endMinutes,
        allDay = event.allDay,
        use24Hour = use24Hour
    )

    GlassCard(
        cornerRadius = 18.dp,
        accentBorderColor = category.accent.copy(alpha = 0.48f),
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(category.accent)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = category.accent
                    )
                    GlassCard(
                        cornerRadius = 50.dp,
                        accentBorderColor = category.accent.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = category.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MistSilver,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = IceWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (showDateBadge) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val date = event.calendarDate()
                    Text(
                        text = "${date.dayOfWeekShortName()} · ${date.shortLabel()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                }

                if (event.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = "Location",
                            tint = SlateMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MistSilver,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (event.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
