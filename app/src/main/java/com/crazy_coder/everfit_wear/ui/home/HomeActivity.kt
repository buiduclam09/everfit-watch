package com.crazy_coder.everfit_wear.ui.home

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.bumptech.glide.Glide
import com.crazy_coder.everfit_wear.R
import com.crazy_coder.everfit_wear.app.Constants
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.google.android.gms.wearable.Wearable
import com.google.android.material.snackbar.Snackbar
import com.crazy_coder.everfit_wear.base.BaseActivity
import com.crazy_coder.everfit_wear.data.model.Exercise
import com.crazy_coder.everfit_wear.databinding.ActivityHomeBinding
import com.crazy_coder.everfit_wear.utils.view.clicks
import com.crazy_coder.everfit_wear.utils.view.gone
import com.crazy_coder.everfit_wear.utils.view.show
import com.google.android.gms.wearable.Node
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var enableBluetooth: ActivityResultLauncher<Intent>? = null
    private var permissionBluetooth: ActivityResultLauncher<String>? = null
    private val listWearConnect = mutableListOf<Node>()
    private val bluetoothPermissions = listOf(
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
    ).let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            it + android.Manifest.permission.BLUETOOTH_CONNECT
        } else {
            it
        }
    }
    private lateinit var timer: CountDownTimer

    private val eventListenMessage: OnMessageReceivedListener by lazy {
        OnMessageReceivedListener {
            String(it.data).apply {
                val keyValueMap = mutableMapOf<String, String>()
                split(",").forEach { pair ->
                    val (key, value) = pair.split(":")
                    keyValueMap[key] = value
                }
                val distance = keyValueMap[Constants.KEY_TOTAL_DISTANCE]
                val heartRate = keyValueMap[Constants.KEY_AVG_HEARTH]
                val calories = keyValueMap[Constants.KEY_TOTAL_CARLO]

                if (distance.isNullOrEmpty()) {
                    binding.tvTotalDistance.text = "$distance km"
                    binding.clContainerInforWatch.show()
                }
                if (heartRate.isNullOrEmpty()) {
                    binding.tvHeartRate.text = "$heartRate bpm"
                    binding.clContainerInforWatch.show()
                }
                if (calories.isNullOrEmpty()) {
                    binding.tvCaloriesValue.text = "$calories cal"
                    binding.clContainerInforWatch.show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionBluetooth = registerForActivityResult(RequestPermission()) {
            enableBluetooth()
        }
        enableBluetooth = registerForActivityResult(StartActivityForResult()) {
            Wearable.getMessageClient(this).addListener(eventListenMessage)
            updateListWearConnect()
        }
        binding.btnSend.clicks {
            listWearConnect.forEach { node ->
                Wearable.getMessageClient(this).sendMessage(
                    node.id,
                    PATH_SEND_DATA,
                    binding.edtMessage.text.toString().toByteArray()
                ).addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Send data to wear",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Send failure: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
        binding.btnStartWorkout.clicks {
            val exercise = exercises[Random(4).nextInt()]
            val data = "${Constants.KEY_TITLE}:${exercise.name},${Constants.KEY_EVENT}:${Constants.KEY_START}"
            sendEvent(data)
            startExercise(exercise)
        }

        binding.btnNextWorkout.clicks {
            val exercise = exercises[Random(4).nextInt()]
            startExercise(exercise)
        }
        binding.btnCompleteWorkout.clicks {
            sendEvent(Constants.KEY_COMPLETE)
        }
        binding.btnRest.clicks {
            val data = "${Constants.KEY_EVENT}:${Constants.KEY_REST}"
            sendEvent(data)
        }

        binding.btnOffRest.clicks {
            val data = "${Constants.KEY_EVENT}:${Constants.KEY_SKIP_REST}"
            sendEvent(data)
        }
    }

    private fun updateListWearConnect() {
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener {
                listWearConnect.clear()
                listWearConnect.addAll(it)
                if (it.isEmpty()) {
                    binding.clSendMess.gone()
                } else {
                    binding.clSendMess.show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        enableBluetooth()
    }

    override fun onDestroy() {
        Wearable.getMessageClient(this).removeListener(eventListenMessage)
        super.onDestroy()
    }

    private fun enableBluetooth() {
        if (bluetoothPermissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                Snackbar.make(binding.root, getString(R.string.not_support_bluetooth), 500).show()
                return
            }
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBluetooth?.launch(enableBtIntent)
            } else {
                Wearable.getMessageClient(this).addListener(eventListenMessage)
                updateListWearConnect()
            }
        } else {
            bluetoothPermissions.firstOrNull { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
                ?.let { permissionBluetooth?.launch(it) }
        }
    }

    private fun startExercise(exercise: Exercise) {
        binding.tvTitleWorkout.text = exercise.name
        binding.tvDescriptionWorkout.text = exercise.description
        Glide.with(this)
            .asGif()
            .load(exercise.gifImageUrl)
            .into(binding.imvGif)
        binding.tvTimerWorkout.text = formatTime(exercise.durationInSeconds)

        timer = object : CountDownTimer(exercise.durationInSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimerWorkout.text = formatTime((millisUntilFinished / 1000).toInt())

            }

            override fun onFinish() {
                binding.btnStartWorkout.text = "Restart Workout"
                binding.imvGif.visibility = View.VISIBLE
                binding.btnStartWorkout.isEnabled = true
            }
        }.start()
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun completeExercise() {
        timer.cancel()
    }

    companion object {
        const val PATH_SEND_DATA = "/data_client"
        private val exercises = mutableListOf(
            Exercise(
                "Push-ups",
                "Place your hands shoulder-width apart on the floor. Lower your body until your chest nearly touches the floor. Push your body back up until your arms are fully extended.",
                300,
                "https://media.tenor.com/gI-8qCUEko8AAAAC/pushup.gif"
            ),
            Exercise(
                "Squats",
                "Stand with your feet shoulder-width apart. Lower your body as far as you can by pushing your hips back and bending your knees. Return to the starting position.",
                450,
                "https://thumbs.gfycat.com/HeftyPartialGroundbeetle-size_restricted.gif"
            ),
            Exercise(
                "Plank",
                "Start in a push-up position, then bend your elbows and rest your weight on your forearms. Hold this position for as long as you can.",
                600,
                "https://media.tenor.com/6SOetkNbfakAAAAM/plank-abs.gif"
            ),
            Exercise(
                "Leg Lifts",
                "Start in a push-up position, then bend your elbows and rest your weight on your forearms. Hold this position for as long as you can.",
                300,
                "https://media.giphy.com/media/xT0BKC0JxPEIkUCvjq/giphy.gif"
            )
        )
    }

    private fun sendEvent(event: String) {
        listWearConnect.forEach { node ->
            Wearable.getMessageClient(this).sendMessage(
                node.id,
                PATH_SEND_DATA,
                event.toByteArray()
            ).addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Send data to wear",
                    Toast.LENGTH_SHORT
                ).show()
            }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Send failure: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}