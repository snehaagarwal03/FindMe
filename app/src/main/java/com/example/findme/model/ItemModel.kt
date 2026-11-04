package com.example.findme.model

import com.example.findme.data.db.ItemEntity
import java.io.Serializable

enum class ItemType {
    LOST, FOUND
}

enum class ItemCategory(val displayName: String) {
    ELECTRONICS("Electronics"),
    BAGS("Bags"),
    ACCESSORIES("Accessories"),
    DOCUMENTS_ID("Documents / ID Cards"),
    BOOKS_STATIONERY("Books / Stationery"),
    CLOTHING("Clothing"),
    OTHER("Other")
}

data class ItemModel(
    val id: String,
    val title: String,
    val type: ItemType,
    val category: ItemCategory,
    val customCategory: String? = null,
    val location: String,
    val date: String,
    val description: String,
    val contactInfo: String? = null,
    val isMyReport: Boolean = false,
    val isResolved: Boolean = false,
    val imageUri: String? = null,
    val imageResId: Int? = null
) : Serializable {
    val displayCategory: String
        get() = if (category == ItemCategory.OTHER && !customCategory.isNullOrBlank()) {
            customCategory
        } else {
            category.displayName
        }
}

/** Converts a Room entity into a UI model. */
fun ItemEntity.toItemModel(): ItemModel = ItemModel(
    id = id,
    title = title,
    type = type,
    category = category,
    customCategory = customCategory,
    location = location,
    date = date,
    description = description,
    contactInfo = contactInfo,
    isMyReport = isMyReport,
    isResolved = isResolved,
    imageUri = imagePath,
    imageResId = imageResId
)
