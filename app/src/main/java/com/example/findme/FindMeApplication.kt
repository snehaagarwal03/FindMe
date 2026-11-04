package com.example.findme

import android.app.Application
import com.example.findme.data.db.FindMeDatabase
import com.example.findme.data.preferences.FindMePreferences
import com.example.findme.data.repository.ItemRepository
import com.example.findme.model.SampleData
import com.example.findme.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FindMeApplication : Application() {

    val database: FindMeDatabase by lazy { FindMeDatabase.getInstance(this) }
    val repository: ItemRepository by lazy { ItemRepository(database.itemDao()) }
    val preferences: FindMePreferences by lazy { FindMePreferences(this) }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Create notification channel early
        NotificationHelper.createNotificationChannel(this)

        // Initialize sample data on first launch only
        applicationScope.launch {
            initSampleDataIfNeeded()
        }
    }

    private suspend fun initSampleDataIfNeeded() {
        val isInitialized = preferences.isSampleDataInitialized.first()
        if (!isInitialized) {
            val sampleItems = SampleData.getSampleItems()
            for (item in sampleItems) {
                repository.insertItemIfNotExists(item)
            }
            preferences.setSampleDataInitialized()
        }
    }
}

