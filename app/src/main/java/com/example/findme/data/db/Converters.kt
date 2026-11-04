package com.example.findme.data.db

import androidx.room.TypeConverter
import com.example.findme.model.ItemCategory
import com.example.findme.model.ItemType

class Converters {

    @TypeConverter
    fun fromItemType(value: ItemType): String = value.name

    @TypeConverter
    fun toItemType(value: String): ItemType = ItemType.valueOf(value)

    @TypeConverter
    fun fromItemCategory(value: ItemCategory): String = value.name

    @TypeConverter
    fun toItemCategory(value: String): ItemCategory = ItemCategory.valueOf(value)
}

