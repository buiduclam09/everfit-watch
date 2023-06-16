package com.crazy_coder.everfit_wear.worker

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log

class SensorDataListener(val onCompleted: (Double) -> Unit) : SensorEventListener {

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("####", "$event")
        event?.values?.firstOrNull()?.let {
            Log.d("####", "Data:$it")
            onCompleted(it.toDouble())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Log.d(this::class.java.simpleName, "onAccuracyChanged: ")
    }
}