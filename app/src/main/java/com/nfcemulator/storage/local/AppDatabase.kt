package com.nfcemulator.storage.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TagEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
}
