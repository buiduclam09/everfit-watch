package com.crazy_coder.everfit_wear.ui.home

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.bumptech.glide.Glide
import com.crazy_coder.everfit_wear.R
import com.crazy_coder.everfit_wear.app.Constants
import com.crazy_coder.everfit_wear.base.BaseActivity
import com.crazy_coder.everfit_wear.data.model.DataWorkout
import com.crazy_coder.everfit_wear.data.model.EventWorkout
import com.crazy_coder.everfit_wear.data.model.Exercise
import com.crazy_coder.everfit_wear.data.model.Rest
import com.crazy_coder.everfit_wear.databinding.ActivityHomeBinding
import com.crazy_coder.everfit_wear.utils.view.clicks
import com.crazy_coder.everfit_wear.utils.view.gone
import com.crazy_coder.everfit_wear.utils.view.show
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

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
    private var timer: CountDownTimer? = null
    var exercise: Exercise? = null

    private val eventListenMessage: OnMessageReceivedListener by lazy {
        OnMessageReceivedListener {
            String(it.data).apply {
                val gson = Gson()
                val receivedString = String(it.data, Charsets.UTF_8)
                val receivedEvent = gson.fromJson(receivedString, DataWorkout::class.java)
                Log.d("BBBBB", "$receivedEvent")
                if (receivedEvent.distanceTotal?.isNotEmpty() == true) {
                    binding.tvTotalDistance.text = "${receivedEvent.distanceTotal}"
                    binding.clContainerInforWatch.show()
                }
                if (receivedEvent.avgHeart?.isNotEmpty() == true) {
                    binding.tvHeartRate.text = "${receivedEvent.avgHeart} bpm"
                    binding.clContainerInforWatch.show()
                }
                if (receivedEvent.calories?.isNotEmpty() == true) {
                    binding.tvCaloriesValue.text = "${receivedEvent.calories}"
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
            exercise = exercises.random()
            val data = EventWorkout(title = exercise?.name, event = Constants.KEY_START)
            passDataWakeUpApp(data)
            startExercise(exercise ?: exercises[0])
        }

        binding.btnWakeUp.clicks {
            exercise = exercises.random()
            val data = EventWorkout(title = exercise?.name, event = Constants.KEY_START)
            wakeUpApp(data)
        }

        binding.btnNextWorkout.clicks {
            completeExercise()
            exercise = exercises.random()
            val data = EventWorkout(title = exercise?.name, event = Constants.KEY_NEXT)
            passDataWakeUpApp(data)
            sendEvent(data)
            startExercise(exercise ?: exercises[0])
        }
        binding.btnCompleteWorkout.clicks {
            completeExercise()
            val data = EventWorkout(title = exercise?.name, event = Constants.KEY_COMPLETE)
            passDataWakeUpApp(data)
            sendEvent(data)
        }
        binding.btnRest.clicks {
            val data = EventWorkout(title = exercise?.name, event = Constants.KEY_REST)
            passDataWakeUpApp(data)
            startRest(rests[0])
            sendEvent(data)
        }

        binding.btnOffRest.clicks {
            val data = EventWorkout(title = exercise?.name, event = Constants.KEY_SKIP_REST)
            passDataWakeUpApp(data)
            skipRest()
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
        binding.imvGif.visibility = View.VISIBLE
        binding.tvTitleWorkout.text = exercise.name
        binding.tvDescriptionWorkout.text = exercise.description
        Glide.with(this@HomeActivity)
            .asGif()
            .load(exercise.gifImageUrl)
            .into(binding.imvGif)

        binding.tvTimerWorkout.text = formatTime(exercise.durationInSeconds)

        timer = object : CountDownTimer(exercise.durationInSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimerWorkout.text = formatTime((millisUntilFinished / 1000).toInt())

            }

            override fun onFinish() {
                binding.btnStartWorkout.text = "Start Workout"
                binding.imvGif.visibility = View.GONE
                binding.btnStartWorkout.isEnabled = true
            }
        }.start()
    }


    private fun startRest(rest: Rest) {
        binding.imvGif.visibility = View.VISIBLE
        binding.tvTitleWorkout.text = rest.name
        Glide.with(this@HomeActivity)
            .asGif()
            .load(rest.gifImageUrl)
            .into(binding.imvGif)

        binding.tvTimerWorkout.text = formatTime(rest.durationInSeconds)

        timer = object : CountDownTimer(rest.durationInSeconds * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvTimerWorkout.text = formatTime((millisUntilFinished / 1000).toInt())

            }

            override fun onFinish() {
                binding.imvGif.visibility = View.GONE
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
        binding.imvGif.gone()
        timer?.cancel()
    }

    private fun skipRest() {
        binding.imvGif.gone()
        timer?.cancel()
    }

    companion object {
        const val PATH_SEND_DATA = "/data_client"
        private val exercises = mutableListOf(
            Exercise(
                "Push-ups",
                "Place your hands shoulder-width apart on the floor. Lower your body until your chest nearly touches the floor. Push your body back up until your arms are fully extended.",
                300,
                "https://media.giphy.com/media/xTiTnlS1f0DlwzCkko/giphy.gif"
            ),
            Exercise(
                "Squats",
                "Stand with your feet shoulder-width apart. Lower your body as far as you can by pushing your hips back and bending your knees. Return to the starting position.",
                450,
                "https://media.giphy.com/media/IAocXiLUK4Y8t28IKC/giphy.gif"
            ),
            Exercise(
                "Plank",
                "Start in a push-up position, then bend your elbows and rest your weight on your forearms. Hold this position for as long as you can.",
                600,
                "https://media.giphy.com/media/xIQKDKVYvtipesfWFD/giphy.gif"
            ),
            Exercise(
                "Leg Lifts",
                "Start in a push-up position, then bend your elbows and rest your weight on your forearms. Hold this position for as long as you can.",
                300,
                "https://media.giphy.com/media/xT0BKC0JxPEIkUCvjq/giphy.gif"
            )
        )

        private val rests = mutableListOf(
            Rest(
                "Rest",
                300,
                "https://media.giphy.com/media/KD8Ldwzx90X9hi9QHW/giphy.gif"
            ),
        )
    }

    private fun sendEvent(event: EventWorkout) {
        val gson = Gson()
        val eventString = gson.toJson(event)
        val eventByteArray = eventString.toByteArray(Charsets.UTF_8)
        listWearConnect.forEach { node ->
            Wearable.getMessageClient(this).sendMessage(
                node.id,
                PATH_SEND_DATA,
                eventByteArray
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

    @SuppressLint("VisibleForTests")
    private fun passDataWakeUpApp(event: EventWorkout) {
        val gson = Gson()
        val dataEventSend = gson.toJson(event)
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/path_to_data").run {
            dataMap.putString(Constants.DATA_RESULT_KEY, dataEventSend)  // Or any other destination
            asPutDataRequest()
        }
        Wearable.getDataClient(this).putDataItem(putDataReq).addOnSuccessListener {
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

    private fun wakeUpApp(event: EventWorkout) {
        val gson = Gson()
        val dataEventSend = gson.toJson(event)
        val putDataReq: PutDataRequest = PutDataMapRequest.create("/wake_up").run {
            dataMap.putString(Constants.DATA_RESULT_KEY, dataEventSend)  // Or any other destination
            asPutDataRequest()
        }
        Wearable.getDataClient(this).putDataItem(putDataReq).addOnSuccessListener {
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