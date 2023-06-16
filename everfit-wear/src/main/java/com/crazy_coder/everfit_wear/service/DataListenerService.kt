package com.crazy_coder.everfit_wear.service

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class DataListenerService : WearableListenerService() {
    private val TAG = "######## wear: "

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEventBuffer: DataEventBuffer) {
        super.onDataChanged(dataEventBuffer)

        dataEventBuffer.forEach { dataEvent ->
            Log.d(
                TAG,
                """
                          received data >>
                          URI ${dataEvent.dataItem.uri}
                          PATH ${dataEvent.dataItem.uri.path}
                          MESSAGE ${
                    DataMapItem.fromDataItem(
                        dataEvent.dataItem
                    ).dataMap.getString("actionHandshakeRequest")
                }
                      """.trimIndent()
            )
            if (dataEvent.type == DataEvent.TYPE_CHANGED) {
            }
        }
    }
}