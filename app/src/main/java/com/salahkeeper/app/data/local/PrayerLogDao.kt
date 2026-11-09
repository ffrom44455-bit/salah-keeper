package com.salahkeeper.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: PrayerLogEntity): Long

    @Update
    suspend fun update(log: PrayerLogEntity)

    @Query("SELECT * FROM prayer_logs WHERE dateString = :dateString")
    fun getLogsForDate(dateString: String): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs WHERE dateString = :dateString AND prayerName = :prayerName LIMIT 1")
    suspend fun getLogForPrayer(dateString: String, prayerName: String): PrayerLogEntity?

    @Query("SELECT * FROM prayer_logs ORDER BY scheduledTimeMillis DESC")
    fun getAllLogs(): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs WHERE dateString BETWEEN :startDate AND :endDate ORDER BY scheduledTimeMillis ASC")
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs WHERE status = 'MISSED' AND isQazaFulfilled = 0 ORDER BY scheduledTimeMillis DESC")
    fun getUnfulfilledQazaList(): Flow<List<PrayerLogEntity>>

    @Query("UPDATE prayer_logs SET isQazaFulfilled = 1, qazaFulfilledTimeMillis = :timeMillis, status = 'MADE_UP' WHERE id = :id")
    suspend fun markQazaFulfilled(id: Long, timeMillis: Long)

    @Query("DELETE FROM prayer_logs")
    suspend fun clearAll()
}
