package com.crazy_coder.everfit_wear.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
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

    val latestSteps: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[LATEST_STEPS] ?: 0L
    }

    suspend fun storeLatestSteps(steps: Long) {
        context.dataStore.edit { pref ->
            pref[LATEST_STEPS] = steps
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
        private val LATEST_STEPS = longPreferencesKey("latest_steps")
        private val LATEST_CALORIES = doublePreferencesKey("latest_calories")
    }
}

