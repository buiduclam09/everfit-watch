package com.crazy_coder.everfit_wear.service

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.crazy_coder.everfit_wear.presentation.runworkout.RunWorkoutActivity
import com.crazy_coder.everfit_wear.utils.Constants.DATA_RESULT_KEY
import com.crazy_coder.everfit_wear.utils.Constants.KEY_NAVIGATE_DESTINATION
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService


//todo check issue same data not emit
class DataLayerListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/path_to_data") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val destination = dataMap.getString(DATA_RESULT_KEY)
                // Broadcast or send the data to your Composable or ViewModel
                // For simplicity, let's assume you're using a Broadcast:
                Log.e("BBBBBB","$destination")
                val intent = Intent(KEY_NAVIGATE_DESTINATION)
                intent.putExtra(DATA_RESULT_KEY, destination)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
//            when {
//                event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/path_to_data" -> {
//                    val intent = Intent(KEY_NAVIGATE_DESTINATION)
//                    intent.putExtra(DATA_RESULT_KEY, destination)
//                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//                }
//
//                event.dataItem.uri.path == "/wake_up" -> {
//                    Log.d("BBBBBB", "${event.dataItem.uri.path}")
//                    openActivity(RunWorkoutActivity::class.java)
//                }
//            }
        }
    }
    private fun openActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}