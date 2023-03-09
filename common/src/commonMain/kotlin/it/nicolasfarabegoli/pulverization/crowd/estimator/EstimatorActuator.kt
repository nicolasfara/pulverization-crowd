package it.nicolasfarabegoli.pulverization.crowd.estimator

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Actuator
import it.nicolasfarabegoli.pulverization.core.ActuatorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
data class RGB(val red: Int, val green: Int, val blue: Int)

class EstimatorActuator(private val rgbFlow: MutableSharedFlow<RGB>) : Actuator<RGB> {
    override suspend fun actuate(payload: RGB) {
        rgbFlow.emit(payload)
    }
}

class EstimatorActuatorsContainer(private val rgbFlow: MutableSharedFlow<RGB>) : ActuatorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        this += EstimatorActuator(rgbFlow).apply { initialize() }
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
