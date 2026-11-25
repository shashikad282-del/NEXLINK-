package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.firebase.OperationType
import com.example.data.firebase.handleFirestoreError
import com.example.data.model.CalendarEvent
import com.example.data.model.EventCategory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CalendarRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeEvents(userId: String): Flow<List<CalendarEvent>> = flow {
        require(userId.isNotBlank()) { "User ID must not be blank." }
        val path = "users/$userId/events"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("events")
                .orderBy("dateEpochDay", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CalendarEvent::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedWith(
                        compareBy<CalendarEvent>(
                            { it.dateEpochDay },
                            { !it.allDay },
                            { it.startMinutes },
                            { it.title.lowercase() }
                        )
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createEvent(
        title: String,
        dateEpochDay: Long,
        startMinutes: Long,
        endMinutes: Long,
        allDay: Boolean,
        category: EventCategory,
        location: String,
        notes: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/events"
        return try {
            val uid = requireUserId()
            val cleanTitle = title.trim()
            val cleanLocation = location.trim()
            val cleanNotes = notes.trim()
            require(cleanTitle.length in 1..120) { "Title must be between 1 and 120 characters." }
            require(cleanLocation.length <= 120) { "Location must be 120 characters or fewer." }
            require(cleanNotes.length <= 500) { "Notes must be 500 characters or fewer." }
            val safeStart = if (allDay) 0L else startMinutes.coerceIn(0L, 1439L)
            val safeEnd = if (allDay) 1440L else endMinutes.coerceIn(safeStart, 1440L)

            val docRef = db.collection("users").document(uid).collection("events").document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "dateEpochDay" to dateEpochDay,
                "startMinutes" to safeStart,
                "endMinutes" to safeEnd,
                "allDay" to allDay,
                "category" to category.name,
                "location" to cleanLocation,
                "notes" to cleanNotes,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateEvent(
        eventId: String,
        title: String,
        dateEpochDay: Long,
        startMinutes: Long,
        endMinutes: Long,
        allDay: Boolean,
        category: EventCategory,
        location: String,
        notes: String
    ): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/events/$eventId"
        return try {
            val uid = requireUserId()
            require(eventId.isNotBlank()) { "Event ID must not be blank." }
            val cleanTitle = title.trim()
            val cleanLocation = location.trim()
            val cleanNotes = notes.trim()
            require(cleanTitle.length in 1..120) { "Title must be between 1 and 120 characters." }
            require(cleanLocation.length <= 120) { "Location must be 120 characters or fewer." }
            require(cleanNotes.length <= 500) { "Notes must be 500 characters or fewer." }
            val safeStart = if (allDay) 0L else startMinutes.coerceIn(0L, 1439L)
            val safeEnd = if (allDay) 1440L else endMinutes.coerceIn(safeStart, 1440L)

            val docRef = db.collection("users").document(uid).collection("events").document(eventId)
            docPath = docRef.path
            docRef.update(
                mapOf(
                    "title" to cleanTitle,
                    "dateEpochDay" to dateEpochDay,
                    "startMinutes" to safeStart,
                    "endMinutes" to safeEnd,
                    "allDay" to allDay,
                    "category" to category.name,
                    "location" to cleanLocation,
                    "notes" to cleanNotes,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String): Result<Unit> {
        var docPath = "users/${auth.currentUser?.uid ?: "unauthenticated"}/events/$eventId"
        return try {
            val uid = requireUserId()
            require(eventId.isNotBlank()) { "Event ID must not be blank." }
            val docRef = db.collection("users").document(uid).collection("events").document(eventId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
