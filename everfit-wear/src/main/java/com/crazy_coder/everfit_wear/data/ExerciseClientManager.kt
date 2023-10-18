package com.crazy_coder.everfit_wear.data

import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.ExerciseUpdateCallback
import androidx.health.services.client.HealthServicesClient
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.ComparisonType
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeCondition
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseGoal
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.LocationAvailability
import androidx.health.services.client.data.WarmUpConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

/**
 * Entry point for [HealthServicesClient] APIs, wrapping them in coroutine-friendly APIs.
 */

class ExerciseClientManager @Inject constructor(
    healthServicesClient: HealthServicesClient,
    coroutineScope: CoroutineScope
) {
    private val exerciseClientService = healthServicesClient.exerciseClient
    private var exerciseCapabilities: ExerciseTypeCapabilities? = null
    private var capabilitiesLoaded = false

    suspend fun getExerciseCapabilities(): ExerciseTypeCapabilities? {
        val capabilities = exerciseClientService.getCapabilitiesAsync().await()
        if (!capabilitiesLoaded) {
            if (ExerciseType.RUNNING in capabilities.supportedExerciseTypes) {
                exerciseCapabilities =
                    capabilities.getExerciseTypeCapabilities(ExerciseType.RUNNING)
            }
        }
        return exerciseCapabilities
    }

    suspend fun isExerciseInProgress(): Boolean {
        val exerciseInfo = exerciseClientService.getCurrentExerciseInfoAsync().await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS
    }

    suspend fun isTrackingExerciseInAnotherApp(): Boolean {
        val exerciseInfo = exerciseClientService.getCurrentExerciseInfoAsync().await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OTHER_APP_IN_PROGRESS

    }

    private fun supportsCalorieGoal(capabilities: ExerciseTypeCapabilities): Boolean {
        val supported = capabilities.supportedGoals[DataType.CALORIES_TOTAL]
        return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
    }

    private fun supportsDistanceMilestone(capabilities: ExerciseTypeCapabilities): Boolean {
        val supported = capabilities.supportedMilestones[DataType.DISTANCE_TOTAL]
        return supported != null && ComparisonType.GREATER_THAN_OR_EQUAL in supported
    }

    suspend fun startExercise() {
        Log.d(OUTPUT, "Starting exercise")
        // Types for which we want to receive metrics. Only ask for ones that are supported.
        val capabilities = getExerciseCapabilities() ?: return
        val dataTypes = setOf(
            DataType.HEART_RATE_BPM,
            DataType.HEART_RATE_BPM_STATS,
            DataType.CALORIES_TOTAL,
            DataType.DISTANCE_TOTAL,
        ).intersect(capabilities.supportedDataTypes)
        val exerciseGoals = mutableListOf<ExerciseGoal<Double>>()
        if (supportsCalorieGoal(capabilities)) {
            // Create a one-time goal.
            exerciseGoals.add(
                ExerciseGoal.createOneTimeGoal(
                    DataTypeCondition(
                        dataType = DataType.CALORIES_TOTAL,
                        threshold = CALORIES_THRESHOLD,
                        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                    )
                )
            )
        }

        if (supportsDistanceMilestone(capabilities)) {
            // Create a milestone goal. To make a milestone for every kilometer, set the initial
            // threshold to 1km and the period to 1km.
            exerciseGoals.add(
                ExerciseGoal.createMilestone(
                    condition = DataTypeCondition(
                        dataType = DataType.DISTANCE_TOTAL,
                        threshold = DISTANCE_THRESHOLD,
                        comparisonType = ComparisonType.GREATER_THAN_OR_EQUAL
                    ), period = DISTANCE_THRESHOLD
                )
            )
        }

        val config = ExerciseConfig(
            exerciseType = ExerciseType.RUNNING,
            dataTypes = dataTypes,
            isAutoPauseAndResumeEnabled = false,
            isGpsEnabled = true,
            exerciseGoals = exerciseGoals
        )
        exerciseClientService.startExerciseAsync(config).await()
    }

    /***
     * Note: don't call this method from outside of ExerciseService.kt
     * when acquiring calories or distance.
     */
    suspend fun prepareExercise() {
        Log.d(OUTPUT, "Preparing an exercise")
        val warmUpConfig = WarmUpConfig(
            ExerciseType.RUNNING, setOf(
                DataType.HEART_RATE_BPM, DataType.LOCATION
            )
        )
        try {
            exerciseClientService.prepareExerciseAsync(warmUpConfig).await()
        } catch (e: Exception) {
            Log.e(OUTPUT, "Prepare exercise failed - ${e.message}")
        }
    }

    suspend fun endExercise() {
        Log.d(OUTPUT, "Ending exercise")
        exerciseClientService.endExerciseAsync().await()
    }

    suspend fun pauseExercise() {
        Log.d(OUTPUT, "Pausing exercise")
        exerciseClientService.pauseExerciseAsync().await()
    }

    suspend fun resumeExercise() {
        Log.d(OUTPUT, "Resuming exercise")
        exerciseClientService.resumeExerciseAsync().await()
    }

    /** Wear OS 3.0 reserves two buttons for the OS. For devices with more than 2 buttons,
     * consider implementing a "press" to mark lap feature**/
    suspend fun markLap() {
        if (isExerciseInProgress()) {
            exerciseClientService.markLapAsync().await()
        }
    }

    /**
     * When the flow starts, it will register an [ExerciseUpdateCallback] and start to emit
     * messages. When there are no more subscribers, or when the coroutine scope is
     * cancelled, this flow will unregister the listener.
     * [callbackFlow] is used to bridge between a callback-based API and Kotlin flows.
     */
    val exerciseUpdateFlow = callbackFlow {
        val callback = object : ExerciseUpdateCallback {
            override fun onExerciseUpdateReceived(update: ExerciseUpdate) {
                coroutineScope.runCatching {
                    trySendBlocking(ExerciseMessage.ExerciseUpdateMessage(update))
                }
            }

            override fun onLapSummaryReceived(lapSummary: ExerciseLapSummary) {
                coroutineScope.runCatching {
                    trySendBlocking(ExerciseMessage.LapSummaryMessage(lapSummary))
                }
            }

            override fun onRegistered() {
            }

            override fun onRegistrationFailed(throwable: Throwable) {
                TODO("Not yet implemented")
            }

            override fun onAvailabilityChanged(
                dataType: DataType<*, *>, availability: Availability
            ) {
                if (availability is LocationAvailability) {
                    coroutineScope.runCatching {
                        trySendBlocking(ExerciseMessage.LocationAvailabilityMessage(availability))
                    }
                }
            }
        }
        exerciseClientService.setUpdateCallback(callback)
        awaitClose {
            exerciseClientService.clearUpdateCallbackAsync(callback)
        }
    }


    private companion object {
        const val CALORIES_THRESHOLD = 250.0
        const val DISTANCE_THRESHOLD = 1_000.0 // meters
        const val OUTPUT = "Output"

    }
}


sealed class ExerciseMessage {
    class ExerciseUpdateMessage(val exerciseUpdate: ExerciseUpdate) : ExerciseMessage()
    class LapSummaryMessage(val lapSummary: ExerciseLapSummary) : ExerciseMessage()
    class LocationAvailabilityMessage(val locationAvailability: LocationAvailability) :
        ExerciseMessage()
}



