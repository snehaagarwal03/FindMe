package com.example.findme.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "findme_prefs")

class FindMePreferences(private val context: Context) {

    companion object {
        private val KEY_LAST_FILTER = stringPreferencesKey("last_filter")
        private val KEY_SAMPLE_DATA_INITIALIZED = booleanPreferencesKey("sample_data_initialized")

        // Filter values
        const val FILTER_ALL = "ALL"
        const val FILTER_LOST = "LOST"
        const val FILTER_FOUND = "FOUND"
    }

    /** Returns the user's last selected filter (ALL, LOST, or FOUND). */
    val lastFilter: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_FILTER] ?: FILTER_ALL
    }

    /** Saves the user's currently selected filter preference. */
    suspend fun saveLastFilter(filter: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_FILTER] = filter
        }
    }

    /** Returns whether sample data has been inserted into the database. */
    val isSampleDataInitialized: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SAMPLE_DATA_INITIALIZED] ?: false
    }

    /** Marks sample data as initialized. */
    suspend fun setSampleDataInitialized() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SAMPLE_DATA_INITIALIZED] = true
        }
    }
}

