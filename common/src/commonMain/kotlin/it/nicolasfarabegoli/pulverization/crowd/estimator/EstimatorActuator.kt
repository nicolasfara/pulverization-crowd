package it.nicolasfarabegoli.pulverization.crowd.estimator

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Actuator
import it.nicolasfarabegoli.pulverization.core.ActuatorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
data class RGB(val red: Double, val green: Double, val blue: Double)

class EstimatorActuator : Actuator<RGB> {
    override suspend fun actuate(payload: RGB) {
        TODO("Not yet implemented")
    }
}

class EstimatorActuatorsContainer : ActuatorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        this += EstimatorActuator().apply { initialize() }
    }
}

suspend fun estimatorActuatorLogic(
    actuator: ActuatorsContainer,
    behaviour: BehaviourRef<RGB>,
) {
    actuator.get<EstimatorActuator> {
        behaviour.receiveFromComponent().collect {
            actuate(it)
        }
    }
}
