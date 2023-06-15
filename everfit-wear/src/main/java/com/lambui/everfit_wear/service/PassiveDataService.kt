package com.lambui.everfit_wear.service

import android.widget.Toast
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import com.lambui.everfit_wear.data.PassiveDataRepository
import com.lambui.everfit_wear.utils.Constants
import com.lambui.everfit_wear.utils.latestHeartRate
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Service to receive data from Health Services.
 *
 * Passive data is delivered from Health Services to this service. Override the appropriate methods
 * in [PassiveListenerService] to receive updates for new data points, goals achieved etc.
 */
@AndroidEntryPoint
class PassiveDataService : PassiveListenerService() {
    @Inject
    lateinit var repository: PassiveDataRepository

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        runBlocking {
            dataPoints.getData(DataType.HEART_RATE_BPM).latestHeartRate()?.let {
                repository.storeLatestHeartRate(it)
                sendHeartRateToPhone(it)
            }
            dataPoints.getData(DataType.STEPS).let {
                repository.storeLatestSteps(it)
            }
        }
    }

    private fun sendHeartRateToPhone(heartRate: Double) {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                Wearable.getMessageClient(this)
                    .sendMessage(
                        node.id,
                        Constants.PATH_SEND_DATA,
                        "${DataType.HEART_RATE_BPM.name}:$heartRate".toByteArray()
                    ).addOnSuccessListener {
                        Toast.makeText(this, "Send data to phone", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Send failure: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }

    private fun sendStepToPhone(heartRate: Long) {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                Wearable.getMessageClient(this)
                    .sendMessage(
                        node.id,
                        Constants.PATH_SEND_DATA,
                        "${DataType.HEART_RATE_BPM.name}:$heartRate".toByteArray()
                    ).addOnSuccessListener {
                        Toast.makeText(this, "Send data to phone", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Send failure: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }
}
