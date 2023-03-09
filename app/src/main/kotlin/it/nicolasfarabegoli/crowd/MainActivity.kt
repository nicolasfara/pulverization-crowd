package it.nicolasfarabegoli.crowd

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.ToggleButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

/**
 * TODO.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var btHandler: BluetoothHandler
    private lateinit var pulverizationManager: PulverizationManager
    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val isBluetoothEnabled: Boolean
        get() {
            val btAdapter = bluetoothManager.adapter ?: return false
            return btAdapter.isEnabled
        }
    private val enableBluetoothRequest =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> startLogic()
                else -> askToEnableBluetooth()
            }
        }

    // UI elements
    private val deviceIdText by lazy { findViewById<EditText>(R.id.deviceIdText) }
    private val ipText by lazy { findViewById<EditText>(R.id.ipText) }
    private val startButton by lazy { findViewById<Button>(R.id.startButton) }
    private val offloadToggle by lazy { findViewById<Switch>(R.id.offloadToggle) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton.isEnabled = false
        deviceIdText.doOnTextChanged { text, _, _, _ -> startButton.isEnabled = text.toString().trim().isNotEmpty() }

        startButton.setOnClickListener {
            Log.i(this::class.simpleName, "Start platform")
            if (deviceIdText.text.toString().trim().isEmpty() || ipText.text.toString().trim().isEmpty()) {
                Log.w("MainActivity", "Please, select platform IP and device ID.")
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    title = "Fill all the fields"
                    setMessage("Please fill the Platform IP and Device ID")
                    setPositiveButton(android.R.string.ok) { _, _ -> }
                }
                builder.show()
            } else {
                startLogic()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (bluetoothManager.adapter != null) {
            if (!isBluetoothEnabled) {
                askToEnableBluetooth()
            }
        } else {
            Log.e(this::class.simpleName, "This device has not bluetooth hardware")
        }
    }

    private fun startLogic() {
        checkPermissions {
            btHandler = BluetoothHandler.getInstance(applicationContext, deviceIdText.text.toString()).apply { start() }
            pulverizationManager = PulverizationManager(
                lifecycle,
                lifecycleScope,
                ipText.text.toString(),
                deviceIdText.text.toString(),
                offloadToggle.isChecked,
                btHandler.rssiFlow(),
            )
            lifecycle.addObserver(pulverizationManager)
            pulverizationManager.runPlatform()
            Log.i(this::class.simpleName, "Start pulverization")
        }
    }

    private fun askToEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothRequest.launch(enableBtIntent)
    }
}
