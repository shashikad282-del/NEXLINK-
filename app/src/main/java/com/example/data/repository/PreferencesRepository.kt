package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.glassPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "glass_calendar_prefs"
)

data class CalendarPreferences(
    val use24HourFormat: Boolean = false,
    val weekStartsOnSunday: Boolean = false
)

class PreferencesRepository(private val context: Context) {
    private val appContext = context.applicationContext

    val preferences: Flow<CalendarPreferences> = appContext.glassPrefsDataStore.data.map { prefs ->
        CalendarPreferences(
            use24HourFormat = prefs[KEY_24H_FORMAT] ?: false,
            weekStartsOnSunday = prefs[KEY_WEEK_SUNDAY] ?: false
        )
    }

    suspend fun setUse24HourFormat(enabled: Boolean) {
        appContext.glassPrefsDataStore.edit { prefs ->
            prefs[KEY_24H_FORMAT] = enabled
        }
    }

    suspend fun setWeekStartsOnSunday(enabled: Boolean) {
        appContext.glassPrefsDataStore.edit { prefs ->
            prefs[KEY_WEEK_SUNDAY] = enabled
        }
    }

    private companion object {
        val KEY_24H_FORMAT = booleanPreferencesKey("use_24_hour_format")
        val KEY_WEEK_SUNDAY = booleanPreferencesKey("week_starts_on_sunday")
    }
}
