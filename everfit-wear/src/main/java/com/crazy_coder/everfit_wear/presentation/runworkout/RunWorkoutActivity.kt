package com.crazy_coder.everfit_wear.presentation.runworkout

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.crazy_coder.everfit_wear.data.model.EventWorkout
import com.crazy_coder.everfit_wear.presentation.ExerciseSampleApp
import com.crazy_coder.everfit_wear.presentation.route.Screens
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.crazy_coder.everfit_wear.utils.Constants
import com.crazy_coder.everfit_wear.utils.Constants.KEY_COMPLETE
import com.crazy_coder.everfit_wear.utils.Constants.KEY_NEXT
import com.crazy_coder.everfit_wear.utils.Constants.KEY_REST
import com.crazy_coder.everfit_wear.utils.Constants.KEY_SKIP_REST
import com.crazy_coder.everfit_wear.utils.Constants.KEY_START


@AndroidEntryPoint
class RunWorkoutActivity : ComponentActivity() {
    private lateinit var navController: NavHostController
    private val exerciseViewModel by viewModels<ExerciseViewModel>()
    private val eventListenMessage: MessageClient.OnMessageReceivedListener by lazy {
        MessageClient.OnMessageReceivedListener { it ->
            Log.e("BBBBB", "${it.data}")
            Toast.makeText(this, "From phone: $it", Toast.LENGTH_SHORT).show()
            String(it.data).apply {
                val gson = Gson()
                val receivedString = String(it.data, Charsets.UTF_8)
                val receivedEvent =
                    gson.fromJson(receivedString, EventWorkout::class.java) ?: return@OnMessageReceivedListener
                Log.e("BBBBBB","$receivedEvent")
                val intent = Intent(Constants.KEY_NAVIGATE_DESTINATION)
                intent.putExtra(Constants.DATA_RESULT_KEY, receivedString)
                LocalBroadcastManager.getInstance(this@RunWorkoutActivity).sendBroadcast(intent)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            /** Check if we have an active exercise. If true, set our destination as the
             * Exercise Screen. If false, route to preparing a new exercise. **/

            //todo handle start screen when receive data app
            val destination = when (exerciseViewModel.isExerciseInProgress()) {
                false -> Screens.StartingUp.route
                true -> Screens.ExerciseScreen.route
            }

            setContent {
                navController = rememberSwipeDismissableNavController()
                ExerciseSampleApp(
                    navController,
                    startDestination = destination
                )
            }
        }

        setContent {
            MyApp()
        }
    }

    private fun navigationWhenReceiveEvent(receivedEvent: EventWorkout) {
        lifecycleScope.launch {
            val destination = when (receivedEvent.event) {
                KEY_START -> {
                    Screens.StartingUp.route
                }

                KEY_NEXT -> {
                    Screens.ExerciseScreen.route
                }

                KEY_COMPLETE -> {
                    Screens.SummaryScreen.route
                }

                KEY_REST -> {
                    Screens.ExerciseScreen.route
                }

                KEY_SKIP_REST -> {
                    Screens.ExerciseScreen.route
                }

                else -> {
                    if (exerciseViewModel.isExerciseInProgress()) {
                        Screens.ExerciseScreen.route
                    } else {
                        Screens.StartingUp.route
                    }
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(eventListenMessage)
    }

    override fun onPause() {
        Wearable.getMessageClient(this).removeListener(eventListenMessage)
        super.onPause()
    }
}

@Composable
fun MyApp() {
    Text(text = "This is the RunWorkoutActivity")
}

