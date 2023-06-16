package com.crazy_coder.everfit_wear.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.crazy_coder.everfit_wear.worker.CalculatorDataWorker
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "passive_data")

fun Context.startWorker() {
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        CalculatorDataWorker::class.java.simpleName,
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
        PeriodicWorkRequest.Builder(
            CalculatorDataWorker::class.java,
            16,
            TimeUnit.MINUTES
        ).build()
    )
}