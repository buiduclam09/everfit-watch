/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crazy_coder.everfit_wear.presentation


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.services.client.data.LocationAvailability
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Confirmation
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import com.crazy_coder.everfit_wear.R
import com.crazy_coder.everfit_wear.data.ServiceState
import com.crazy_coder.everfit_wear.data.model.EventWorkout
import com.crazy_coder.everfit_wear.presentation.route.Screens
import com.crazy_coder.everfit_wear.presentation.runworkout.ExerciseNotAvailable
import com.crazy_coder.everfit_wear.presentation.runworkout.ExerciseScreen
import com.crazy_coder.everfit_wear.presentation.runworkout.ExerciseViewModel
import com.crazy_coder.everfit_wear.presentation.runworkout.PreparingExercise
import com.crazy_coder.everfit_wear.presentation.runworkout.StartingUp
import com.crazy_coder.everfit_wear.presentation.runworkout.SummaryScreen
import com.crazy_coder.everfit_wear.utils.Constants.DATA_RESULT_KEY
import com.crazy_coder.everfit_wear.utils.Constants.KEY_COMPLETE
import com.crazy_coder.everfit_wear.utils.Constants.KEY_NAVIGATE_DESTINATION
import com.crazy_coder.everfit_wear.utils.Constants.KEY_PRE_START
import com.crazy_coder.everfit_wear.utils.Constants.KEY_REST
import com.crazy_coder.everfit_wear.utils.Constants.KEY_SKIP_REST
import com.crazy_coder.everfit_wear.utils.Constants.KEY_START
import com.crazy_coder.everfit_wear.utils.Constants.KEY_UNKNOWN
import com.google.gson.Gson
import kotlin.random.Random


/** Navigation for the exercise app. **/

@Composable
fun ExerciseSampleApp(
    navController: NavHostController,
    startDestination: String
) {
    val viewModel = hiltViewModel<ExerciseViewModel>()
    val context = LocalContext.current
    val destination = remember { mutableStateOf(EventWorkout("", KEY_UNKNOWN, Random.nextLong())) }
    val showDialog = remember { mutableStateOf(false) }
    val service by viewModel.exerciseServiceState
    var isAcceptGps = false
    if (service is ServiceState.Connected) {
        val locationFlow = (service as ServiceState.Connected).locationAvailabilityState
        val location by locationFlow.collectAsStateWithLifecycle()
        isAcceptGps =
            location == LocationAvailability.ACQUIRED_TETHERED || location == LocationAvailability.ACQUIRED_UNTETHERED

    }
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == KEY_NAVIGATE_DESTINATION) {
                    val dataReceive = intent.getStringExtra(DATA_RESULT_KEY) ?: ""
                    val gson = Gson()
                    val destinationObj = gson.fromJson(dataReceive, EventWorkout::class.java)
                    destination.value = destinationObj
                    Log.e("BBBBB", "Destination ${destinationObj}")
                }
            }
        }

        LocalBroadcastManager.getInstance(context)
            .registerReceiver(receiver, IntentFilter(KEY_NAVIGATE_DESTINATION))

        onDispose {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }
    LaunchedEffect(destination.value) {
        Log.e(
            "BBBBB",
            "Destination EverfitWearApp ${navController.currentDestination?.route} -- event ${destination.value.event}"
        )
        when (destination.value.event) {
            KEY_START -> {
                if (isAcceptGps) {
                    navController.navigate(Screens.StartingUp.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                } else {
                    navController.navigate(Screens.ExerciseScreen.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            }

            KEY_PRE_START -> navController.navigate(Screens.PreparingExercise.route) {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }

            KEY_COMPLETE -> {
                if (navController.currentDestination?.route == Screens.ExerciseScreen.route) {
                    viewModel.endExercise()
                    Log.e("BBBBB", "BBBBBB${viewModel.state.value}")
                    ///{averageHeartRate}/{totalDistance}/{totalCalories}/{elapsedTime}
                    navController.navigate(Screens.SummaryScreen.route + "/{${viewModel.state.value.avgHeart}}/{${viewModel.state.value.distance}}/{${viewModel.state.value.calories}}/{${viewModel.state.value.claps}}") {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                } else {
                    return@LaunchedEffect
                }
            }


            KEY_REST -> {
                if (navController.currentDestination?.route == Screens.ExerciseScreen.route) {
                    showDialog.value = true
                } else {
                    return@LaunchedEffect
                }
            }

            KEY_SKIP_REST -> {
                if (navController.currentDestination?.route == Screens.ExerciseScreen.route) {
                    if (showDialog.value) {  // Check if the dialog is showing
                        showDialog.value = false // If yes, close the dialog
                    }
                } else {
                    return@LaunchedEffect
                }
            }
        }
    }
    SwipeDismissableNavHost(
        navController = navController, startDestination = startDestination
    ) {
        composable(Screens.StartingUp.route) {
            val viewModel = hiltViewModel<ExerciseViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            StartingUp(onAvailable = {
                navController.navigate(Screens.PreparingExercise.route) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }, onUnavailable = {
                navController.navigate(Screens.ExerciseNotAvailable.route) {
                    popUpTo(navController.graph.id) {
                        inclusive = false
                    }
                }
            }, hasCapabilities = uiState.hasExerciseCapabilities

            )
        }
        composable(Screens.PreparingExercise.route) {
            val viewModel = hiltViewModel<ExerciseViewModel>()
            val serviceState by viewModel.exerciseServiceState
            val permissions = viewModel.permissions
            val uiState by viewModel.uiState.collectAsState()
            PreparingExercise(
                onStartClick = {
                    navController.navigate(Screens.ExerciseScreen.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = false
                        }
                    }

                },
                prepareExercise = { viewModel.prepareExercise() },
                onStart = { viewModel.startExercise() },
                serviceState = serviceState,
                permissions = permissions,
                isTrackingAnotherExercise = uiState.isTrackingAnotherExercise,
            )
        }
        composable(Screens.ExerciseScreen.route) {
            val serviceState by viewModel.exerciseServiceState
            ExerciseScreen(
                onPauseClick = { viewModel.pauseExercise() },
                onEndClick = { viewModel.endExercise() },
                onResumeClick = { viewModel.resumeExercise() },
                onStartClick = { viewModel.startExercise() },
                serviceState = serviceState,
                navController = navController,
                isShowRestTime = false,
                viewModel = viewModel
            )
        }
        composable(Screens.ExerciseNotAvailable.route) {
            ExerciseNotAvailable()
        }
        composable(
            Screens.SummaryScreen.route + "/{averageHeartRate}/{totalDistance}/{totalCalories}/{elapsedTime}",
            arguments = listOf(navArgument("averageHeartRate") { type = NavType.StringType },
                navArgument("totalDistance") { type = NavType.StringType },
                navArgument("totalCalories") { type = NavType.StringType },
                navArgument("elapsedTime") { type = NavType.StringType })
        ) {
            SummaryScreen(
                averageHeartRate = it.arguments?.getString("averageHeartRate")!!,
                totalDistance = it.arguments?.getString("totalDistance")!!,
                totalCalories = it.arguments?.getString("totalCalories")!!,
                elapsedTime = it.arguments?.getString("elapsedTime")!!,
                onRestartClick = {
                    navController.navigate(Screens.StartingUp.route) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
                viewModel
            )
        }
    }
    if (showDialog.value) {
        Dialog(
            showDialog = true,
            onDismissRequest = {
                showDialog.value = false
            }
        ) {
            Confirmation(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = stringResource(R.string.confirmation_dialog_tick),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                showDialog.value = false
                            }
                    )
                },
                onTimeout = {
                },
            ) {
                Text(
                    text = stringResource(R.string.ending_timer),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}




