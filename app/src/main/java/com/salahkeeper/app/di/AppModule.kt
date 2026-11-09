package com.salahkeeper.app.di

import android.content.Context
import com.salahkeeper.app.data.local.PrayerLogDao
import com.salahkeeper.app.data.repository.LocationRepository
import com.salahkeeper.app.data.repository.PrayerRepository
import com.salahkeeper.app.data.repository.SettingsRepository
import com.salahkeeper.app.data.service.AdhanCalculationService
import com.salahkeeper.app.data.service.AlarmScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent)
object AppModule {

    @Provides
    @Singleton
    fun providePrayerRepository(
        prayerLogDao: PrayerLogDao,
        adhanCalculationService: AdhanCalculationService,
        settingsRepository: SettingsRepository,
        locationRepository: LocationRepository,
        alarmScheduler: AlarmScheduler
    ): PrayerRepository {
        return PrayerRepository(
            prayerLogDao = prayerLogDao,
            calculationService = adhanCalculationService,
            settingsRepository = settingsRepository,
            locationRepository = locationRepository,
            alarmScheduler = alarmScheduler
        )
    }
}
