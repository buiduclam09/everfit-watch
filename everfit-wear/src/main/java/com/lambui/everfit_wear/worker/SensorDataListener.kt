package com.lambui.everfit_wear.worker

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener

class SensorDataListener(val onCompleted: (Double) -> Unit) : SensorEventListener {

    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.firstOrNull()?.let {
            onCompleted(it.toDouble())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Log.d(this::class.java.simpleName, "onAccuracyChanged: ")
    }
}