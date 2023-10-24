package com.crazy_coder.everfit_wear.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.crazy_coder.everfit_wear.utils.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PassiveDataRepository @Inject constructor(@ApplicationContext val context: Context) {


    val latestCalories: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[LATEST_CALORIES] ?: 0.0
    }

    suspend fun storeLatestCalories(calories: Double) {
        context.dataStore.edit { pref ->
            pref[LATEST_CALORIES] = calories
        }
    }

    val latestClap: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LATEST_CLAPS] ?: 0
    }

    suspend fun storeLatestClap(latest: Long) {
        context.dataStore.edit { pref ->
            pref[LATEST_CLAPS] = latest
        }
    }

    val latestDistances: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LATEST_DISTANCE] ?: ""
    }

    suspend fun storeLatestDistances(distances: String) {
        context.dataStore.edit { pref ->
            pref[LATEST_DISTANCE] = distances
        }
    }

    val latestHeartRate: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[LATEST_HEART_RATE] ?: 0.0
    }

    suspend fun storeLatestHeartRate(heartRate: Double) {
        context.dataStore.edit { prefs ->
            prefs[LATEST_HEART_RATE] = heartRate
        }
    }


    val latestTemperature: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[LATEST_TEMPERATURE] ?: 0.0
    }

    suspend fun storeLatestTemperature(temperature: Double) {
        context.dataStore.edit { prefs ->
            prefs[LATEST_TEMPERATURE] = temperature
        }
    }

    companion object {
        private val LATEST_HEART_RATE = doublePreferencesKey("latest_heart_rate")
        private val LATEST_TEMPERATURE = doublePreferencesKey("latest_temperature")
        private val LATEST_DISTANCE = stringPreferencesKey("latest_distances")
        private val LATEST_CLAPS = longPreferencesKey("latest_claps")
        private val LATEST_CALORIES = doublePreferencesKey("latest_calories")
    }
}

