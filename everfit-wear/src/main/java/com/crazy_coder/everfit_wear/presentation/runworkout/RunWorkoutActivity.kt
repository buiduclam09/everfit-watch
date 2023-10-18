package com.crazy_coder.everfit_wear.presentation.runworkout

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.crazy_coder.everfit_wear.presentation.ExerciseSampleApp
import com.crazy_coder.everfit_wear.presentation.route.Screens
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class RunWorkoutActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    private val exerciseViewModel by viewModels<ExerciseViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            Log.e("AAAAAAAAAAAAA", "${task.result}")
            task.result?.let {
                exerciseViewModel.updateToken(it)
            }
        })
        lifecycleScope.launch {

            /** Check if we have an active exercise. If true, set our destination as the
             * Exercise Screen. If false, route to preparing a new exercise. **/
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
}

@Composable
fun MyApp() {
    Text(text = "This is the RunWorkoutActivity")
}
