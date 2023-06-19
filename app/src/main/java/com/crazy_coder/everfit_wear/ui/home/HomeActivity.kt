package com.crazy_coder.everfit_wear.ui.home

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.crazy_coder.everfit_wear.R
import com.google.android.gms.wearable.MessageClient.OnMessageReceivedListener
import com.google.android.gms.wearable.Wearable
import com.google.android.material.snackbar.Snackbar
import com.crazy_coder.everfit_wear.base.BaseActivity
import com.crazy_coder.everfit_wear.databinding.ActivityHomeBinding
import com.crazy_coder.everfit_wear.utils.view.gone
import com.crazy_coder.everfit_wear.utils.view.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var enableBluetooth: ActivityResultLauncher<Intent>? = null
    private var permissionBluetooth: ActivityResultLauncher<String>? = null
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

    private val eventListenMessage: OnMessageReceivedListener by lazy {
        OnMessageReceivedListener {
            String(it.data).split(":").apply {
                val key = firstOrNull()
                val value = lastOrNull()
                when (key) {
                    Sensor.TYPE_HEART_RATE.toString() -> {
                        binding.tvHeartRate.text = value
                        binding.clContainerInforWatch.show()
                        binding.clContainerConnectWatch.gone()
                    }
                    Sensor.TYPE_AMBIENT_TEMPERATURE.toString() -> {
                        binding.tvTemperature.text = value
                        binding.clContainerInforWatch.show()
                        binding.clContainerConnectWatch.gone()
                    }
                    else -> Unit
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
            }
        } else {
            bluetoothPermissions.firstOrNull { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
                ?.let { permissionBluetooth?.launch(it) }
        }
    }
}