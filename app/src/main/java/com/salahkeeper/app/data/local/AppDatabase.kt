package com.salahkeeper.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PrayerLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerLogDao(): PrayerLogDao
}
