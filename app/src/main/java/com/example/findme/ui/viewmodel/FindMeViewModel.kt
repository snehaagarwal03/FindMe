package com.example.findme.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.findme.FindMeApplication
import com.example.findme.data.db.ItemEntity
import com.example.findme.data.preferences.FindMePreferences
import com.example.findme.model.ItemType
import com.example.findme.utils.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FindMeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FindMeApplication
    private val repository = app.repository
    private val preferences = app.preferences

    // --- Home Screen State (Active Reports Only) ---

    /** Current search query text */
    val searchQuery = MutableStateFlow("")

    /** Current filter: ALL, LOST, or FOUND */
    val currentFilter = MutableStateFlow(FindMePreferences.FILTER_ALL)

    /** Observable list of ACTIVE items for Home, filtered by type and search query */
    val homeItems: Flow<List<ItemEntity>> = combine(
        searchQuery, currentFilter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        if (query.isBlank()) {
            when (filter) {
                FindMePreferences.FILTER_LOST -> repository.getActiveItemsByType(ItemType.LOST)
                FindMePreferences.FILTER_FOUND -> repository.getActiveItemsByType(ItemType.FOUND)
                else -> repository.getActiveItems()
            }
        } else {
            // When searching, search active items
            repository.searchActiveItems(query)
        }
    }

    // --- My Reports State (Active vs Past Tabs) ---

    enum class MyReportsTab {
        ACTIVE, PAST
    }

    val myReportsTab = MutableStateFlow(MyReportsTab.ACTIVE)

    /** Observable list of locally submitted reports (Active or Past based on selected tab) */
    val myReports: Flow<List<ItemEntity>> = myReportsTab.flatMapLatest { tab ->
        when (tab) {
            MyReportsTab.ACTIVE -> repository.getMyActiveReports()
            MyReportsTab.PAST -> repository.getMyPastReports()
        }
    }

    fun setMyReportsTab(tab: MyReportsTab) {
        myReportsTab.value = tab
    }

    // --- Completion Workflow ---

    fun markReportCompleted(itemId: String) {
        viewModelScope.launch {
            repository.updateCompletionStatus(itemId, true)
        }
    }

    // --- Report Submission ---

    sealed class SubmitResult {
        data class Success(val item: ItemEntity) : SubmitResult()
        data class Error(val message: String) : SubmitResult()
    }

    private val _submitResult = MutableSharedFlow<SubmitResult>()
    val submitResult = _submitResult.asSharedFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun submitReport(item: ItemEntity) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                repository.insertItem(item)

                // Show notification after successful database insertion
                NotificationHelper.showReportSubmittedNotification(
                    getApplication(),
                    item.id,
                    item.title,
                    item.type
                )

                _submitResult.emit(SubmitResult.Success(item))
            } catch (e: Exception) {
                _submitResult.emit(
                    SubmitResult.Error("Failed to save report: ${e.localizedMessage ?: "Unknown error"}")
                )
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    // --- Item Detail ---

    fun getItemByIdFlow(itemId: String): Flow<ItemEntity?> = repository.getItemByIdFlow(itemId)

    // --- Filter Preferences ---

    fun setFilter(filter: String) {
        currentFilter.value = filter
        viewModelScope.launch {
            preferences.saveLastFilter(filter)
        }
    }

    fun loadSavedFilter() {
        viewModelScope.launch {
            preferences.lastFilter.collect { savedFilter ->
                currentFilter.value = savedFilter
            }
        }
    }
}
