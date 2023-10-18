package com.crazy_coder.everfit_wear.worker

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.crazy_coder.everfit_wear.worker.Spo2Status.MEASUREMENT_COMPLETED
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTracker.TrackerError
import com.samsung.android.service.health.tracking.HealthTracker.TrackerEventListener
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey

class SpO2Listener(val onCompleted: (Double) -> Unit) {
    private val spo2Handler = Handler(Looper.getMainLooper())
    private val spo2Listener: TrackerEventListener = object : TrackerEventListener {
        override fun onDataReceived(list: List<DataPoint>) {
            for (data in list) {
                updateSpo2(data)
            }
        }

        override fun onFlushCompleted() {
            Log.i(TAG, "Flush completed")
        }

        override fun onError(trackerError: TrackerError) {
            Log.i(TAG, "SpO2 Tracker error: $trackerError")
            if (trackerError == TrackerError.PERMISSION_ERROR) {
                //ObserverUpdater.getObserverUpdater().displayError(R.string.NoPermission);
            }
            if (trackerError == TrackerError.SDK_POLICY_ERROR) {
                //ObserverUpdater.getObserverUpdater().displayError(R.string.SDKPolicyError);
            }
        }
    }
    private var isHandlerRunning = false
    private var spo2Tracker: HealthTracker? = null

    fun init(healthTrackingService: HealthTrackingService) {
        spo2Tracker = healthTrackingService.getHealthTracker(HealthTrackerType.SPO2)
    }

    fun startTracker() {
        if (!isHandlerRunning) {
            spo2Handler.post { spo2Tracker?.setEventListener(spo2Listener) }
            isHandlerRunning = true
        }
    }

    fun stopTracker() {
        if (spo2Tracker != null) spo2Tracker?.unsetEventListener()
        spo2Handler.removeCallbacksAndMessages(null)
        isHandlerRunning = false
    }

    private fun updateSpo2(data: DataPoint) {
        val status = data.getValue(ValueKey.SpO2Set.STATUS)
        var spo2Value = 0
        if (status == MEASUREMENT_COMPLETED) spo2Value = data.getValue(ValueKey.SpO2Set.SPO2)
        onCompleted(spo2Value.toDouble())
    }

    companion object {
        private const val TAG = "SpO2 Listener"
    }
}