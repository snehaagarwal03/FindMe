package com.example.findme.data.repository

import com.example.findme.data.db.ItemDao
import com.example.findme.data.db.ItemEntity
import com.example.findme.model.ItemType
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {

    fun getAllItems(): Flow<List<ItemEntity>> = itemDao.getAllItems()

    fun getMyReports(): Flow<List<ItemEntity>> = itemDao.getMyReports()

    fun getItemsByType(type: ItemType): Flow<List<ItemEntity>> = itemDao.getItemsByType(type)

    fun searchItems(query: String): Flow<List<ItemEntity>> = itemDao.searchItems(query)

    fun getItemByIdFlow(itemId: String): Flow<ItemEntity?> = itemDao.getItemByIdFlow(itemId)

    suspend fun getItemById(itemId: String): ItemEntity? = itemDao.getItemById(itemId)

    suspend fun insertItem(item: ItemEntity) = itemDao.insertItem(item)

    suspend fun insertItemIfNotExists(item: ItemEntity) = itemDao.insertItemIfNotExists(item)

    suspend fun updateItem(item: ItemEntity) = itemDao.updateItem(item)

    suspend fun deleteItem(item: ItemEntity) = itemDao.deleteItem(item)

    suspend fun deleteItemById(itemId: String) = itemDao.deleteItemById(itemId)

    suspend fun getItemCount(): Int = itemDao.getItemCount()
}

