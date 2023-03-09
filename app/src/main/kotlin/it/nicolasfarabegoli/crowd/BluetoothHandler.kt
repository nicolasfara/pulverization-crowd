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
import com.welie.blessed.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*

class BluetoothHandler private constructor(private val context: Context, private val deviceId: String) {
    private val rssiChannel = MutableSharedFlow<Pair<String, Int>>(1)
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
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM).build()

        private val advData = AdvertiseData.Builder()
            .setIncludeTxPowerLevel(true)
            .addServiceUuid(ParcelUuid(UUID.randomUUID()))
            .build()
        private val advResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true).build()

        private val regexDeviceName = "^crowd-\\d+\$".toRegex()

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context, deviceId: String): BluetoothHandler {
            if (instance == null) {
                instance = BluetoothHandler(context, deviceId)
            }
            return requireNotNull(instance)
        }
    }

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

    suspend fun stop() {
        observerJob.cancelAndJoin()
        phManager.stopAdvertising()
    }

    fun rssiFlow(): Flow<Pair<String, Int>> = rssiChannel.asSharedFlow()

    private fun startScanning() {
        btCentralManager.scanForPeripherals(
            { bluetoothPeripheral, scanResult ->
                if (bluetoothPeripheral.name.matches(regexDeviceName)) {
                    val (deviceName) = regexDeviceName.find(bluetoothPeripheral.name)!!.destructured
                    Log.i(TAG, "Found '${deviceName}' with RSSI ${scanResult.rssi}")
                    scope.launch { rssiChannel.emit(deviceName to scanResult.rssi) }
                }
            },
            {
                Log.e(TAG, "Fail scan with reason $it")
            }
        )
    }
}
