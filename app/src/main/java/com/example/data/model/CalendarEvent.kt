package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class CalendarEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val dateEpochDay: Long = 0L,
    val startMinutes: Long = 540L,
    val endMinutes: Long = 600L,
    val allDay: Boolean = false,
    val category: String = EventCategory.WORK.name,
    val location: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun resolvedCategory(): EventCategory = EventCategory.fromRaw(category)

    fun calendarDate(): CalendarDate = CalendarDate.fromEpochDay(dateEpochDay)
}
