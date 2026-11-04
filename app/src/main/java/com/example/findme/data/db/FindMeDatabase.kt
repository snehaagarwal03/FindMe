package com.example.findme.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ItemEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FindMeDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: FindMeDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE items ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): FindMeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FindMeDatabase::class.java,
                    "findme_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build().also { INSTANCE = it }
            }
        }
    }
}
