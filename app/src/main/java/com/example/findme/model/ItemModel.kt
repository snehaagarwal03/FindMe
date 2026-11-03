package com.example.findme.model

import java.io.Serializable

enum class ItemType {
    LOST, FOUND
}

enum class ItemCategory(val displayName: String) {
    ELECTRONICS("Electronics"),
    ID_CARDS("ID & Cards"),
    BOOKS_NOTES("Books & Notes"),
    KEYS("Keys"),
    CLOTHING("Clothing & Accessories"),
    OTHER("Other")
}

data class ItemModel(
    val id: String,
    val title: String,
    val type: ItemType,
    val category: ItemCategory,
    val location: String,
    val date: String,
    val description: String,
    val contactInfo: String,
    val isMyReport: Boolean = false,
    val isResolved: Boolean = false
) : Serializable

