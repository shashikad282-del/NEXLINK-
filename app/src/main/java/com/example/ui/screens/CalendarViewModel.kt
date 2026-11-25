package com.example.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.model.CalendarDate
import com.example.data.model.CalendarEvent
import com.example.data.model.EventCategory
import com.example.data.repository.CalendarPreferences
import com.example.data.repository.CalendarRepository
import com.example.data.repository.PreferencesRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class EventEditorRequest(
    val existingEvent: CalendarEvent? = null,
    val initialDate: CalendarDate = CalendarDate.today()
)

class CalendarViewModel(
    private val calendarRepository: CalendarRepository,
    private val preferencesRepository: PreferencesRepository,
    private val currentUserId: String
) : ViewModel() {

    val eventsState: StateFlow<UiState<List<CalendarEvent>>> =
        calendarRepository.observeEvents(currentUserId)
            .map<List<CalendarEvent>, UiState<List<CalendarEvent>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing calendar events", rootError)
                emit(UiState.Error(rootError.localizedMessage ?: "Failed to sync calendar events."))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val preferences: StateFlow<CalendarPreferences> =
        preferencesRepository.preferences.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CalendarPreferences()
        )

    private val _selectedDate = MutableStateFlow(CalendarDate.today())
    val selectedDate: StateFlow<CalendarDate> = _selectedDate.asStateFlow()

    private val _displayedMonth = MutableStateFlow(CalendarDate.today().firstOfMonth())
    val displayedMonth: StateFlow<CalendarDate> = _displayedMonth.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow<EventCategory?>(null)
    val categoryFilter: StateFlow<EventCategory?> = _categoryFilter.asStateFlow()

    private val _editorRequest = MutableStateFlow<EventEditorRequest?>(null)
    val editorRequest: StateFlow<EventEditorRequest?> = _editorRequest.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun selectDate(date: CalendarDate) {
        _selectedDate.value = date
        if (date.year != _displayedMonth.value.year || date.month != _displayedMonth.value.month) {
            _displayedMonth.value = date.firstOfMonth()
        }
    }

    fun stepMonth(deltaMonths: Int) {
        _displayedMonth.value = _displayedMonth.value.plusMonths(deltaMonths).firstOfMonth()
    }

    fun jumpToToday() {
        val today = CalendarDate.today()
        _selectedDate.value = today
        _displayedMonth.value = today.firstOfMonth()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategoryFilter(category: EventCategory?) {
        _categoryFilter.value = category
    }

    fun openNewEventEditor(date: CalendarDate = _selectedDate.value) {
        _editorRequest.value = EventEditorRequest(existingEvent = null, initialDate = date)
    }

    fun openEditEventEditor(event: CalendarEvent) {
        _editorRequest.value = EventEditorRequest(
            existingEvent = event,
            initialDate = event.calendarDate()
        )
    }

    fun closeEditor() {
        if (!_isSaving.value) {
            _editorRequest.value = null
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun saveEvent(
        existingId: String?,
        title: String,
        date: CalendarDate,
        startMinutes: Long,
        endMinutes: Long,
        allDay: Boolean,
        category: EventCategory,
        location: String,
        notes: String
    ) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            val result = if (existingId.isNullOrBlank()) {
                calendarRepository.createEvent(
                    title = title,
                    dateEpochDay = date.toEpochDay(),
                    startMinutes = startMinutes,
                    endMinutes = endMinutes,
                    allDay = allDay,
                    category = category,
                    location = location,
                    notes = notes
                )
            } else {
                calendarRepository.updateEvent(
                    eventId = existingId,
                    title = title,
                    dateEpochDay = date.toEpochDay(),
                    startMinutes = startMinutes,
                    endMinutes = endMinutes,
                    allDay = allDay,
                    category = category,
                    location = location,
                    notes = notes
                )
            }
            _isSaving.value = false
            result.onSuccess {
                _selectedDate.value = date
                _displayedMonth.value = date.firstOfMonth()
                _editorRequest.value = null
                _statusMessage.value = if (existingId.isNullOrBlank()) {
                    "Event added to schedule"
                } else {
                    "Event updated"
                }
            }.onFailure { err ->
                _statusMessage.value = err.localizedMessage ?: "Unable to save event"
            }
        }
    }

    fun deleteEvent(eventId: String) {
        if (_isSaving.value || eventId.isBlank()) return
        viewModelScope.launch {
            _isSaving.value = true
            val result = calendarRepository.deleteEvent(eventId)
            _isSaving.value = false
            result.onSuccess {
                _editorRequest.value = null
                _statusMessage.value = "Event removed"
            }.onFailure { err ->
                _statusMessage.value = err.localizedMessage ?: "Unable to delete event"
            }
        }
    }

    fun setUse24HourFormat(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setUse24HourFormat(enabled)
        }
    }

    fun setWeekStartsOnSunday(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setWeekStartsOnSunday(enabled)
        }
    }

    companion object {
        private const val TAG = "CalendarViewModel"
        private const val STOP_TIMEOUT_MILLIS = 5000L

        fun factory(currentUserId: String) = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                CalendarViewModel(
                    calendarRepository = CalendarRepository(db),
                    preferencesRepository = PreferencesRepository(app),
                    currentUserId = currentUserId
                )
            }
        }
    }
}
