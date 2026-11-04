package com.example.findme.model

import com.example.findme.R

object SampleData {
    private val items = mutableListOf(
        ItemModel(
            id = "1",
            title = "MacBook Pro 14\" M2 - Space Gray",
            type = ItemType.LOST,
            category = ItemCategory.ELECTRONICS,
            location = "Library 2nd Floor Study Desk #24",
            date = "Nov 2, 2026",
            description = "Left on study desk near the north window. Has an Octocat sticker on the lid and a small scratch on the bottom casing.",
            contactInfo = "john.doe@campus.edu",
            isMyReport = true,
            imageResId = R.drawable.ic_sample_laptop
        ),
        ItemModel(
            id = "2",
            title = "Blue Hydro Flask 32oz",
            type = ItemType.FOUND,
            category = ItemCategory.ACCESSORIES,
            location = "Campus Recreation Gym Locker Room",
            date = "Nov 3, 2026",
            description = "Blue insulated water bottle found by locker 42. Features a campus engineering sticker. Handed over to the gym reception desk.",
            contactInfo = "gym-desk@campus.edu",
            isMyReport = false,
            imageResId = R.drawable.ic_sample_water_bottle
        ),
        ItemModel(
            id = "3",
            title = "Student ID Card - Sarah Connor",
            type = ItemType.LOST,
            category = ItemCategory.DOCUMENTS_ID,
            location = "Main Dining Hall / Cafeteria",
            date = "Nov 1, 2026",
            description = "Lost official university student ID card during lunch rush near checkout register 2. Needed urgently for dorm access.",
            contactInfo = "sarah.c@campus.edu",
            isMyReport = true,
            imageResId = R.drawable.ic_sample_id_card
        ),
        ItemModel(
            id = "4",
            title = "Set of 3 Keys with Red Carabiner",
            type = ItemType.FOUND,
            category = ItemCategory.ACCESSORIES,
            location = "Science Quad Bench near Fountain",
            date = "Nov 3, 2026",
            description = "Dorm key, bike lock key, and a silver padlock key attached to a red aluminium carabiner clip.",
            contactInfo = "campus-security@campus.edu",
            isMyReport = false,
            imageResId = R.drawable.ic_sample_keys
        ),
        ItemModel(
            id = "5",
            title = "Sony WH-1000XM4 Noise Cancelling Headphones",
            type = ItemType.LOST,
            category = ItemCategory.ELECTRONICS,
            location = "Auditorium Hall B, Row G",
            date = "Oct 31, 2026",
            description = "Black over-ear headphones in an original black zipper case with an audio cable inside.",
            contactInfo = "alex.w@campus.edu",
            isMyReport = false,
            imageResId = R.drawable.ic_sample_headphones
        ),
        ItemModel(
            id = "6",
            title = "Calculus: Early Transcendentals 9th Ed.",
            type = ItemType.FOUND,
            category = ItemCategory.BOOKS_STATIONERY,
            location = "Math Building Rm 104",
            date = "Nov 2, 2026",
            description = "Hardcover textbook left on the lecture podium after the 10:00 AM session. First name 'David' handwritten on inner cover.",
            contactInfo = "prof-math@campus.edu",
            isMyReport = false,
            imageResId = R.drawable.ic_sample_book
        )
    )

    fun getAllItems(): List<ItemModel> = items.toList()

    fun getMyReports(): List<ItemModel> = items.filter { it.isMyReport }

    fun addItem(item: ItemModel) {
        items.add(0, item)
    }
}
