package com.example.findme.model

import com.example.findme.R
import com.example.findme.data.db.ItemEntity

/**
 * Provides initial sample/demo data for the FindMe application.
 * These items are inserted into Room only once on first launch,
 * using DataStore to track whether initialization has occurred.
 */
object SampleData {

    fun getSampleItems(): List<ItemEntity> = listOf(
        ItemEntity(
            id = "sample_1",
            title = "MacBook Pro 14\" M2 - Space Gray",
            type = ItemType.LOST,
            category = ItemCategory.ELECTRONICS,
            location = "Library 2nd Floor Study Desk #24",
            date = "Nov 2, 2026",
            description = "Left on study desk near the north window. Has an Octocat sticker on the lid and a small scratch on the bottom casing.",
            contactInfo = "john.doe@campus.edu",
            isMyReport = false,
            imageResId = R.drawable.ic_sample_laptop,
            createdAt = System.currentTimeMillis() - 6 * 60 * 60 * 1000
        ),
        ItemEntity(
            id = "sample_5",
            title = "Sony WH-1000XM4 Noise Cancelling Headphones",
            type = ItemType.LOST,
            category = ItemCategory.ELECTRONICS,
            location = "Auditorium Hall B, Row G",
            date = "Oct 31, 2026",
            description = "Black over-ear headphones in an original black zipper case with an audio cable inside.",
            contactInfo = "alex.w@campus.edu",
            isMyReport = false,
            imageResId = R.drawable.ic_sample_headphones,
            createdAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000
        )
    )
}
