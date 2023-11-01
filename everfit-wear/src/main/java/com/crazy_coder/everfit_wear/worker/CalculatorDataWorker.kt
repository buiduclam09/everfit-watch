package com.crazy_coder.everfit_wear.worker

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.crazy_coder.everfit_wear.data.PassiveDataRepository
import com.crazy_coder.everfit_wear.utils.Constants
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

@HiltWorker
class CalculatorDataWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: PassiveDataRepository
) : CoroutineWorker(appContext, workerParams) {
    private var sensorManager: SensorManager? = null
    private var numberReadHeartRateFailure = 0
    private var numberReadTemperatureFailure = 0
    private val listNodes = mutableListOf<Node>()
    private var heartRateListener: SensorDataListener? = null
    private var temperatureListener: SensorDataListener? = null
    private var stepsListener: SensorDataListener? = null
    private var spo2Listener: SpO2Listener? = null

    init {
        sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        temperatureListener = SensorDataListener { temperature ->
            if (temperature <= 0) {
                numberReadTemperatureFailure++
            }
            if (numberReadTemperatureFailure >= MAX_READ_TEMPERATURE_FAILURE || temperature > 0) {
                temperatureListener?.let { sensorManager?.unregisterListener(it) }
                numberReadTemperatureFailure = 0
            }
            if (temperature > 0) {
                runBlocking {
                    repository.storeLatestTemperature(temperature)
                    sendDataToPhone(Sensor.TYPE_AMBIENT_TEMPERATURE, temperature)
                }
            }
        }
        heartRateListener = SensorDataListener { heartRate ->
            if (heartRate <= 0) {
                numberReadHeartRateFailure++
            }
            if (numberReadHeartRateFailure >= MAX_READ_HEART_RATE_FAILURE || heartRate > 0) {
                heartRateListener?.let { sensorManager?.unregisterListener(it) }
                numberReadHeartRateFailure = 0
            }
            if (heartRate > 0) {
                runBlocking {
                    repository.storeLatestHeartRate(heartRate)
                    sendDataToPhone(Sensor.TYPE_HEART_RATE, heartRate)
                }
            }
        }

        stepsListener = SensorDataListener { step ->
            if (step > 0) {
                runBlocking {
                    repository.storeLatestSteps(step = step.toLong())
                }
            }
        }
    }

    private fun startMeasure() {
        runCatching {
            numberReadHeartRateFailure = 0
            numberReadTemperatureFailure = 0
            sensorManager?.apply {
                registerListener(
                    heartRateListener,
                    getDefaultSensor(Sensor.TYPE_HEART_RATE),
                    SensorManager.SENSOR_DELAY_FASTEST
                )
                registerListener(
                    temperatureListener,
                    getDefaultSensor(Sensor.TYPE_TEMPERATURE),
                    SensorManager.SENSOR_DELAY_FASTEST
                )
                registerListener(
                    stepsListener,
                    getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_FASTEST
                )
            }
        }
    }

    private fun sendDataToPhone(keyType: Int, data: Double) {
        listNodes.forEach { node ->
            Wearable.getMessageClient(appContext)
                .sendMessage(
                    node.id,
                    Constants.PATH_SEND_DATA,
                    "${keyType}:$data".toByteArray()
                ).addOnSuccessListener {
                    Toast.makeText(
                        appContext,
                        "Send data to phone",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        appContext,
                        "Send failure: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override suspend fun doWork(): Result {
        runBlocking {
            Wearable.getNodeClient(appContext).connectedNodes.addOnSuccessListener { nodes ->
                if (nodes.isNotEmpty()) {
                    listNodes.clear()
                    listNodes.addAll(nodes)

                }
            }
            (1..7).forEach { _ ->
                Log.d("####", "Work")
                startMeasure()
                delay(2 * 60 * 1000)
            }
        }
        return Result.success()
    }

    companion object {
        const val MAX_READ_HEART_RATE_FAILURE = 20
        const val MAX_READ_TEMPERATURE_FAILURE = 5
    }
}
