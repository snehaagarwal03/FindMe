package com.example.findme.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.findme.model.ItemType
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItemIfNotExists(item: ItemEntity)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)

    @Query("SELECT * FROM items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getItemById(itemId: String): ItemEntity?

    @Query("SELECT * FROM items WHERE id = :itemId")
    fun getItemByIdFlow(itemId: String): Flow<ItemEntity?>

    @Query("SELECT * FROM items WHERE isMyReport = 1 ORDER BY createdAt DESC")
    fun getMyReports(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE type = :type ORDER BY createdAt DESC")
    fun getItemsByType(type: ItemType): Flow<List<ItemEntity>>

    @Query("""
        SELECT * FROM items 
        WHERE (title LIKE '%' || :query || '%' 
            OR description LIKE '%' || :query || '%' 
            OR location LIKE '%' || :query || '%' 
            OR category LIKE '%' || :query || '%'
            OR customCategory LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchItems(query: String): Flow<List<ItemEntity>>

    @Query("SELECT COUNT(*) FROM items")
    suspend fun getItemCount(): Int

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: String)
}

