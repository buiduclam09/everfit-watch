package com.crazy_coder.everfit_wear.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.crazy_coder.everfit_wear.data.PassiveDataRepository
import com.crazy_coder.everfit_wear.utils.startWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class StartupReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repository: PassiveDataRepository
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        runBlocking {
            val result = context.checkSelfPermission(PERMISSION)
            if (result == PackageManager.PERMISSION_GRANTED) {
                scheduleWorker(context)
            }
        }
    }

    private fun scheduleWorker(context: Context) {
        context.startWorker()
    }

    companion object {
        const val PERMISSION = android.Manifest.permission.BODY_SENSORS
    }
}

