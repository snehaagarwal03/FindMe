package com.example.findme.data.repository

import com.example.findme.data.db.ItemDao
import com.example.findme.data.db.ItemEntity
import com.example.findme.model.ItemType
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {

    // Active items for Home screen
    fun getActiveItems(): Flow<List<ItemEntity>> = itemDao.getActiveItems()

    fun getActiveItemsByType(type: ItemType): Flow<List<ItemEntity>> = itemDao.getActiveItemsByType(type)

    fun searchActiveItems(query: String): Flow<List<ItemEntity>> = itemDao.searchActiveItems(query)

    // My Reports (Active vs Past)
    fun getMyActiveReports(): Flow<List<ItemEntity>> = itemDao.getMyActiveReports()

    fun getMyPastReports(): Flow<List<ItemEntity>> = itemDao.getMyPastReports()

    fun getMyReports(): Flow<List<ItemEntity>> = itemDao.getMyReports()

    // All items & search (for fallback/admin)
    fun getAllItems(): Flow<List<ItemEntity>> = itemDao.getAllItems()

    fun getItemsByType(type: ItemType): Flow<List<ItemEntity>> = itemDao.getItemsByType(type)

    fun searchItems(query: String): Flow<List<ItemEntity>> = itemDao.searchItems(query)

    // Item detail
    fun getItemByIdFlow(itemId: String): Flow<ItemEntity?> = itemDao.getItemByIdFlow(itemId)

    suspend fun getItemById(itemId: String): ItemEntity? = itemDao.getItemById(itemId)

    // Completion status update
    suspend fun updateCompletionStatus(itemId: String, isCompleted: Boolean) =
        itemDao.updateCompletionStatus(itemId, isCompleted)

    // Standard CRUD
    suspend fun insertItem(item: ItemEntity) = itemDao.insertItem(item)

    suspend fun insertItemIfNotExists(item: ItemEntity) = itemDao.insertItemIfNotExists(item)

    suspend fun updateItem(item: ItemEntity) = itemDao.updateItem(item)

    suspend fun deleteItem(item: ItemEntity) = itemDao.deleteItem(item)

    suspend fun deleteItemById(itemId: String) = itemDao.deleteItemById(itemId)

    suspend fun getItemCount(): Int = itemDao.getItemCount()
}
