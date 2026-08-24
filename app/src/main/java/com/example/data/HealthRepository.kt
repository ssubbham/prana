package com.example.data

import kotlinx.coroutines.flow.Flow

class HealthRepository(
    private val vitalsDao: VitalsDao,
    private val hazardDao: HazardAlertDao,
    private val profileDao: UserProfileDao
) {
    val allVitals: Flow<List<VitalMeasurement>> = vitalsDao.getAllMeasurements()
    val latestVital: Flow<VitalMeasurement?> = vitalsDao.getLatestMeasurement()
    val allHazards: Flow<List<HazardAlert>> = hazardDao.getAllAlerts()
    val userProfile: Flow<UserProfile?> = profileDao.getProfile()

    suspend fun saveVital(measurement: VitalMeasurement): Long {
        return vitalsDao.insertMeasurement(measurement)
    }

    suspend fun getRecentVitals(sinceTimestamp: Long): List<VitalMeasurement> {
        return vitalsDao.getMeasurementsSince(sinceTimestamp)
    }

    suspend fun saveHazard(alert: HazardAlert): Long {
        return hazardDao.insertAlert(alert)
    }

    suspend fun saveProfile(profile: UserProfile) {
        profileDao.saveProfile(profile)
    }

    suspend fun deleteAllVitals() {
        vitalsDao.deleteAll()
    }

    suspend fun deleteVitalById(id: Long) {
        vitalsDao.deleteById(id)
    }
}
