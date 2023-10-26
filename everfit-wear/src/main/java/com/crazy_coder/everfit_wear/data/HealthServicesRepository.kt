package com.crazy_coder.everfit_wear.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.getSystemService
import androidx.health.services.client.data.LocationAvailability
import com.crazy_coder.everfit_wear.R
import com.crazy_coder.everfit_wear.service.ActiveDurationUpdate
import com.crazy_coder.everfit_wear.service.ForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow


class HealthServicesRepository @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {

    @Inject
    lateinit var exerciseClientManager: ExerciseClientManager

    private var exerciseService: ForegroundService? = null

    suspend fun hasExerciseCapability(): Boolean = getExerciseCapabilities() != null

    private suspend fun getExerciseCapabilities() = exerciseClientManager.getExerciseCapabilities()

    suspend fun isExerciseInProgress(): Boolean = exerciseClientManager.isExerciseInProgress()


    suspend fun isTrackingExerciseInAnotherApp() =
        exerciseClientManager.isTrackingExerciseInAnotherApp()


    fun prepareExercise() = exerciseService?.prepareExercise()
    fun startExercise() = exerciseService?.startExercise()
    fun pauseExercise() = exerciseService?.pauseExercise()
    fun endExercise() = exerciseService?.endExercise()
    fun resumeExercise() = exerciseService?.resumeExercise()

    var bound = mutableStateOf(false)

    var serviceState: MutableState<ServiceState> = mutableStateOf(ServiceState.Disconnected)

    private val connection = object : android.content.ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ForegroundService.LocalBinder
            binder.getService().let {
                exerciseService = it
                serviceState.value = ServiceState.Connected(
                    exerciseServiceState = it.exerciseServiceState,
                    locationAvailabilityState = it.locationAvailabilityState,
                    activeDurationUpdate = it.exerciseServiceState.value.exerciseDurationUpdate,
                )
            }
            bound.value = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound.value = false
            exerciseService = null
            serviceState.value = ServiceState.Disconnected
        }

    }

    fun createService() {
        val foregroundServiceIntent = Intent(applicationContext, ForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification channel for the foreground service
            val channel = NotificationChannel("channel_id", "Channel Name", NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(applicationContext,NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)

            // Start the foreground service with a notification
            val notification = Notification.Builder(applicationContext, "channel_id")
                .setContentTitle("Foreground Service")
                .setContentText("Service is running")
                .setSmallIcon(R.drawable.ic_app_notification)
                .build()

//            startForeground(1, notification)
            applicationContext.startForegroundService(foregroundServiceIntent)
        } else {
            applicationContext.startService(foregroundServiceIntent)
        }

        applicationContext.bindService(foregroundServiceIntent, connection, Context.BIND_AUTO_CREATE)
    }

}

/** Store exercise values in the service state. While the service is connected,
 * the values will persist.**/
sealed class ServiceState {
    object Disconnected : ServiceState()
    data class Connected(
        val exerciseServiceState: StateFlow<ForegroundService.ExerciseServiceState>,
        val locationAvailabilityState: StateFlow<LocationAvailability>,
        val activeDurationUpdate: ActiveDurationUpdate?,
    ) : ServiceState()
}






