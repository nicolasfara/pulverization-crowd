package it.nicolasfarabegoli.pulverization.crowd.smartphone

import it.nicolasfarabegoli.pulverization.core.Sensor

typealias NeighboursDistances = Map<String, Double>

class DistanceSensor : Sensor<NeighboursDistances> {
    override suspend fun sense(): NeighboursDistances {
        TODO("Not yet implemented")
    }
}
