package com.crazy_coder.everfit_wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.crazy_coder.everfit_wear.R
import com.crazy_coder.everfit_wear.notification.AppNotificationManager
import com.crazy_coder.everfit_wear.presentation.theme.WearOSTeamKoderTheme
import com.crazy_coder.everfit_wear.service.StartupReceiver.Companion.PERMISSION
import com.crazy_coder.everfit_wear.utils.startWorker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    private val notification by lazy { AppNotificationManager(applicationContext) }
    private val eventListenMessage: MessageClient.OnMessageReceivedListener by lazy {
        MessageClient.OnMessageReceivedListener {
            String(it.data).let {
                Toast.makeText(this, "From phone: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            Log.e("AAAAAAAAAAAAA", "${task.result}")
            task.result?.let {
                viewModel.updateToken(it)
            }
        })
        viewModel.permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { result ->
            viewModel.updateStatusPermission(result)
//            startWorker()
        }
//        runCatching { startWorker() }
        viewModel.updateStatusPermission(checkSelfPermission(PERMISSION) == PackageManager.PERMISSION_GRANTED)
        setContent {
            WearApp(viewModel, notification)
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
fun WearApp(viewModel: MainViewModel, notification: AppNotificationManager) {
    WearOSTeamKoderTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colors.background)
                .verticalScroll(rememberScrollState())
                .selectableGroup(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ever-fit-Wear")
            PermissionToggle(viewModel)
            Spacer(modifier = Modifier.height(10.dp))
            Card(onClick = {}) {
                Text(text = "Last measured")
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row {
                        Image(
                            painter = painterResource(id = R.drawable.ic_heart),
                            contentDescription = ""
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = viewModel.state.value.heartRate
                        )
                    }
                    Row {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_thermostat_auto_24),
                            contentDescription = ""
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = viewModel.state.value.temperature
                        )
                    }

                    Row {
                        Image(
                            painter = painterResource(id = R.drawable.ic_steps),
                            contentDescription = ""
                        )
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = viewModel.state.value.step
                        )
                    }
                    Row {
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(onClick = {
                            notification.showNotification("Chao ae")
                        }) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = "Show"
                            )
                        }
                        Button(onClick = {
                            notification.cancelNotification()
                        }) {
                            Text(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                text = "Hide"
                            )
                        }
                    }

                    Row {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 8.dp),
                            text = viewModel.state.value.token
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun PermissionToggle(viewModel: MainViewModel) {
    val context = LocalContext.current
    AnimatedVisibility(
        visible = viewModel.state.value.showButtonRequest
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            onClick = {
                viewModel.permissionLauncher?.launch(Manifest.permission.BODY_SENSORS)
                Toast.makeText(context, "Request Permission", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,

            ) {
            Text(
                text = "Request permission",
                modifier = Modifier.padding(horizontal = 8.dp),
                fontSize = TextUnit(12f, TextUnitType.Sp)
            )
        }
    }
}