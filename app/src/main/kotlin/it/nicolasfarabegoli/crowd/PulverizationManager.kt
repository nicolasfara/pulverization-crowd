package it.nicolasfarabegoli.crowd

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import it.nicolasfarabegoli.pulverization.crowd.common.*
import it.nicolasfarabegoli.pulverization.crowd.config
import it.nicolasfarabegoli.pulverization.crowd.smartphone.*
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.behaviourLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.communicationLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.sensorsLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.stateLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.*
import kotlinx.coroutines.joinAll

class PulverizationManager(
    private val lifecycle: Lifecycle,
    private val lifeCycleScope: LifecycleCoroutineScope,
    private val platformIp: String,
    private val deviceId: String,
    private val offloadedBehaviour: Boolean,
) : DefaultLifecycleObserver {

    private var canRunThePlatform = false
    private lateinit var platformJob: Job

    companion object {
        private val TAG = PulverizationManager::class.simpleName
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.i(TAG, "Pulverization manager instance created")

        if (canRunThePlatform) {
            Log.i(TAG, "Pulverization platform can start")
            platformJob = lifeCycleScope.launch(Dispatchers.IO) {
                initPulverization()
                Log.i(TAG, "Pulverization platform setup and ready to start")
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        lifeCycleScope.launch(Dispatchers.IO) {
            if (::platformJob.isInitialized) {
                platformJob.cancelAndJoin()
            }
        }
    }

    fun runPlatform() {
        Log.i(TAG, "Setting up the platform")
        canRunThePlatform = true
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            // Start the platform if not started
            platformJob = lifeCycleScope.launch(Dispatchers.IO) {
                initPulverization()
                Log.i(TAG, "Pulverization platform setup and ready to start")
            }
        }
    }

    private suspend fun initPulverization() {
        val platform = pulverizationPlatform(config.getDeviceConfiguration("smartphone")!!) {
            behaviourLogic(SmartphoneBehaviour(), ::smartphoneBehaviourLogic)
            stateLogic(StateComponent(), ::stateComponentLogic)
            communicationLogic(CommunicationComponent(), ::communicationComponentLogic)
            sensorsLogic(SmartphoneSensorsContainer(), ::smartphoneSensorLogic)

            withPlatform { RabbitmqCommunicator() }
            withRemotePlace { defaultRabbitMQRemotePlace() }
            withContext {
                deviceID(deviceId)
            }
        }
        val platformOffloaded = pulverizationPlatform(config.getDeviceConfiguration("smartphone-offloaded")!!) {
            communicationLogic(CommunicationComponent(), ::communicationComponentLogic)
            sensorsLogic(SmartphoneSensorsContainer(), ::smartphoneSensorLogic)

            withPlatform { RabbitmqCommunicator() }
            withRemotePlace { defaultRabbitMQRemotePlace() }
            withContext {
                deviceID(deviceId)
            }
        }

        if (offloadedBehaviour) {
            platformOffloaded.start().joinAll()
            platformOffloaded.stop()
        } else {
            platform.start().joinAll()
            platform.stop()
        }
    }
}