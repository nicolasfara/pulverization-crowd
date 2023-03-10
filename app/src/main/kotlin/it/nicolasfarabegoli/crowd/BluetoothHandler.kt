package it.nicolasfarabegoli.crowd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.welie.blessed.AdvertiseError
import com.welie.blessed.BluetoothCentralManager
import com.welie.blessed.BluetoothPeripheralManager
import com.welie.blessed.BluetoothPeripheralManagerCallback
import com.welie.blessed.ConnectionState
import it.nicolasfarabegoli.pulverization.crowd.smartphone.NeighboursRssi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * TODO.
 */
class BluetoothHandler private constructor(private val context: Context, private val deviceId: String) {
    private val rssiChannel = MutableStateFlow<NeighboursRssi>(emptyMap())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val btCentralManager by lazy { BluetoothCentralManager(context) }
    private lateinit var observerJob: Job
    private lateinit var phManager: BluetoothPeripheralManager
    private val bleCallback = object : BluetoothPeripheralManagerCallback() {
        override fun onAdvertiseFailure(advertiseError: AdvertiseError) {
            Log.e(this::class.simpleName, "Failed to advertise")
        }

        override fun onAdvertisingStarted(settingsInEffect: AdvertiseSettings) {
            Log.i(this::class.simpleName, "Start advertising")
        }

        override fun onAdvertisingStopped() {
            Log.w(this::class.simpleName, "Stop advertising")
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: BluetoothHandler? = null
        private val TAG = BluetoothHandler::class.simpleName

        private val HRS_SERVICE_UUID: UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")

        private val advSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).build()

        private val advData = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .addServiceUuid(ParcelUuid(UUID.randomUUID()))
            .build()
        private val advResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true).build()

        private val regexDeviceName = "^crowd-(\\d+)\$".toRegex()

        /**
         * TODO.
         */
        @JvmStatic
        @Synchronized
        fun getInstance(context: Context, deviceId: String): BluetoothHandler {
            if (instance == null) {
                instance = BluetoothHandler(context, deviceId)
            }
            return requireNotNull(instance)
        }
    }

    /**
     * TODO.
     */
    @SuppressLint("MissingPermission")
    fun start() {
        val bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bleManager.adapter.name = "crowd-$deviceId"
        phManager = BluetoothPeripheralManager(context, bleManager, bleCallback)

        val fakeGattService = BluetoothGattService(HRS_SERVICE_UUID, SERVICE_TYPE_PRIMARY)

        phManager.add(fakeGattService)
        phManager.startAdvertising(advSettings, advData, advResponse)

        Log.i(this::class.simpleName, "Setup advertising")

        btCentralManager.observeConnectionState { peripheral, state ->
            Log.i(TAG, "Peripheral $peripheral is $state")
            when (state) {
                ConnectionState.CONNECTED -> {}
                ConnectionState.DISCONNECTED -> {}
                else -> {}
            }
        }
        btCentralManager.observeAdapterState {
            when (it) {
                BluetoothAdapter.STATE_ON -> startScanning()
            }
        }
        startScanning()
    }

    /**
     * TODO.
     */
    suspend fun stop() {
        observerJob.cancelAndJoin()
        phManager.stopAdvertising()
    }

    /**
     * TODO.
     */
    fun rssiFlow(): Flow<NeighboursRssi> = rssiChannel.asSharedFlow()

    private fun startScanning() {
        btCentralManager.scanForPeripherals(
            { bluetoothPeripheral, scanResult ->
                if (bluetoothPeripheral.name.matches(regexDeviceName)) {
                    regexDeviceName.find(bluetoothPeripheral.name)?.let {
                        val (deviceName) = it.destructured
                        // Log.i(TAG, "Found '$deviceName' with RSSI ${scanResult.rssi}")
                        scope.launch {
                            rssiChannel.update { m -> m + mapOf(deviceName to scanResult.rssi) }
                        }
                    } ?: run { Log.w(TAG, "Fail match regex") }
                }
            },
            {
                Log.e(TAG, "Fail scan with reason $it")
            },
        )
    }
}
