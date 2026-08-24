package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VitalsDao {
    @Query("SELECT * FROM vital_measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<VitalMeasurement>>

    @Query("SELECT * FROM vital_measurements ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMeasurement(): Flow<VitalMeasurement?>

    @Query("SELECT * FROM vital_measurements WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    suspend fun getMeasurementsSince(sinceTimestamp: Long): List<VitalMeasurement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: VitalMeasurement): Long

    @Query("DELETE FROM vital_measurements")
    suspend fun deleteAll()

    @Query("DELETE FROM vital_measurements WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface HazardAlertDao {
    @Query("SELECT * FROM hazard_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<HazardAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: HazardAlert): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alerts: List<HazardAlert>)

    @Query("DELETE FROM hazard_alerts")
    suspend fun clearAlerts()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun getProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)
}
