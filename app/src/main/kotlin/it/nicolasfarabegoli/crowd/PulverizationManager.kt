package it.nicolasfarabegoli.crowd

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import it.nicolasfarabegoli.pulverization.crowd.common.CommunicationComponent
import it.nicolasfarabegoli.pulverization.crowd.common.StateComponent
import it.nicolasfarabegoli.pulverization.crowd.common.communicationComponentLogic
import it.nicolasfarabegoli.pulverization.crowd.common.stateComponentLogic
import it.nicolasfarabegoli.pulverization.crowd.config
import it.nicolasfarabegoli.pulverization.crowd.smartphone.NeighboursRssi
import it.nicolasfarabegoli.pulverization.crowd.smartphone.SmartphoneBehaviour
import it.nicolasfarabegoli.pulverization.crowd.smartphone.SmartphoneSensorsContainer
import it.nicolasfarabegoli.pulverization.crowd.smartphone.smartphoneBehaviourLogic
import it.nicolasfarabegoli.pulverization.crowd.smartphone.smartphoneSensorLogic
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.behaviourLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.communicationLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.sensorsLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.stateLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * TODO.
 */
class PulverizationManager(
    private val lifecycle: Lifecycle,
    private val lifeCycleScope: LifecycleCoroutineScope,
    private val platformIp: String,
    private val deviceId: String,
    private val offloadedBehaviour: Boolean,
    private val rssiFlow: Flow<NeighboursRssi>,
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

    /**
     * TODO.
     */
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
            communicationLogic(CommunicationComponent(platformIp), ::communicationComponentLogic)
            sensorsLogic(SmartphoneSensorsContainer(rssiFlow), ::smartphoneSensorLogic)

            withPlatform { RabbitmqCommunicator(hostname = platformIp) }
            withRemotePlace { defaultRabbitMQRemotePlace() }
            withContext {
                deviceID(deviceId)
            }
        }
        val platformOffloaded = pulverizationPlatform(config.getDeviceConfiguration("smartphone-offloaded")!!) {
            communicationLogic(CommunicationComponent(platformIp), ::communicationComponentLogic)
            sensorsLogic(SmartphoneSensorsContainer(rssiFlow), ::smartphoneSensorLogic)

            withPlatform { RabbitmqCommunicator(hostname = platformIp) }
            withRemotePlace { defaultRabbitMQRemotePlace() }
            withContext {
                deviceID(deviceId)
            }
        }

        try {
            if (offloadedBehaviour) {
                platformOffloaded.start().joinAll()
                platformOffloaded.stop()
            } else {
                platform.start().joinAll()
                platform.stop()
            }
        } catch (ex: IllegalStateException) {
            Log.e(TAG, "Something went wrong: ${ex.message}")
        }
    }
}
