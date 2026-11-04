package com.example.findme.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.findme.model.ItemCategory
import com.example.findme.model.ItemType

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val type: ItemType,
    val category: ItemCategory,
    val customCategory: String? = null,
    val description: String,
    val location: String,
    val date: String,
    val imagePath: String? = null,
    val imageResId: Int? = null,
    val contactInfo: String? = null,
    val isMyReport: Boolean = false,
    val isResolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

