package it.nicolasfarabegoli.pulverization.crowd.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Sensor
import it.nicolasfarabegoli.pulverization.core.SensorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.delay
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds

typealias NeighboursDistances = Map<String, Double>

class DistanceSensor : Sensor<NeighboursDistances> {
    override suspend fun sense(): NeighboursDistances {
        TODO("Not yet implemented")
    }
}

class SmartphoneSensorsContainer : SensorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        this += DistanceSensor().apply { initialize() }
    }
}

suspend fun smartphoneSensorLogic(
    sensor: SensorsContainer,
    behaviourRef: BehaviourRef<NeighboursDistances>,
) {
    while (true) {
        sensor.get<DistanceSensor> {
            behaviourRef.sendToComponent(sense())
        }
        delay(250.milliseconds)
    }
}
