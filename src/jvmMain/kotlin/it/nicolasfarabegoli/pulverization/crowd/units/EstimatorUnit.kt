package it.nicolasfarabegoli.pulverization.crowd.units

import it.nicolasfarabegoli.pulverization.crowd.common.CommunicationComponent
import it.nicolasfarabegoli.pulverization.crowd.common.StateComponent
import it.nicolasfarabegoli.pulverization.crowd.common.communicationComponentLogic
import it.nicolasfarabegoli.pulverization.crowd.common.stateComponentLogic
import it.nicolasfarabegoli.pulverization.crowd.config
import it.nicolasfarabegoli.pulverization.crowd.estimator.EstimatorActuatorsContainer
import it.nicolasfarabegoli.pulverization.crowd.estimator.EstimatorBehaviour
import it.nicolasfarabegoli.pulverization.crowd.estimator.estimatorActuatorLogic
import it.nicolasfarabegoli.pulverization.crowd.estimator.estimatorBehaviourLogic
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.actuatorsLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.behaviourLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.communicationLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.stateLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val platform = pulverizationPlatform(config.getDeviceConfiguration("estimator")!!) {
        behaviourLogic(EstimatorBehaviour(), ::estimatorBehaviourLogic)
        stateLogic(StateComponent(), ::stateComponentLogic)
        communicationLogic(CommunicationComponent(), ::communicationComponentLogic)
        actuatorsLogic(EstimatorActuatorsContainer(), ::estimatorActuatorLogic)
        withPlatform { RabbitmqCommunicator() }
        withRemotePlace { defaultRabbitMQRemotePlace() }
    }
    platform.start().joinAll()
    platform.stop()
}
