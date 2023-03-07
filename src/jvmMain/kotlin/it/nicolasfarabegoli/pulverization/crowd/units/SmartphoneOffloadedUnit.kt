package it.nicolasfarabegoli.pulverization.crowd.units

import it.nicolasfarabegoli.pulverization.crowd.common.StateComponent
import it.nicolasfarabegoli.pulverization.crowd.common.stateComponentLogic
import it.nicolasfarabegoli.pulverization.crowd.config
import it.nicolasfarabegoli.pulverization.crowd.smartphone.SmartphoneBehaviour
import it.nicolasfarabegoli.pulverization.crowd.smartphone.smartphoneBehaviourLogic
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.behaviourLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.stateLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

/**
 * TODO.
 */
fun main() = runBlocking {
    val platform = pulverizationPlatform(config.getDeviceConfiguration("smartphone-offloaded")!!) {
        behaviourLogic(SmartphoneBehaviour(), ::smartphoneBehaviourLogic)
        stateLogic(StateComponent(), ::stateComponentLogic)
        withPlatform { RabbitmqCommunicator() }
        withRemotePlace { defaultRabbitMQRemotePlace() }
    }
    platform.start().joinAll()
    platform.stop()
}
