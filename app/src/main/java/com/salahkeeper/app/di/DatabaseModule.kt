package com.salahkeeper.app.di

import android.content.Context
import androidx.room.Room
import com.salahkeeper.app.data.local.AppDatabase
import com.salahkeeper.app.data.local.PrayerLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "salah_keeper.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun providePrayerLogDao(database: AppDatabase): PrayerLogDao {
        return database.prayerLogDao()
    }
}
