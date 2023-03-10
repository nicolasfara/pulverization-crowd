package it.nicolasfarabegoli.pulverization.crowd.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Sensor
import it.nicolasfarabegoli.pulverization.core.SensorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds

typealias NeighboursRssi = Map<String, Int>

class DistanceSensor(private val neighboursDistance: Flow<NeighboursRssi>) : Sensor<NeighboursRssi> {
    private var filterMap = mapOf<String, MovingAverage<Int>>()
    private val scope = CoroutineScope(SupervisorJob())
    private lateinit var job: Job

    companion object {
        private const val PERIOD = 5
    }

    override suspend fun initialize() {
        job = scope.launch {
            neighboursDistance.collect {
                it.forEach { (k, v) ->
                    filterMap[k]?.add(v) ?: run {
                        filterMap = filterMap + (k to MovingAverage<Int>(PERIOD).apply { add(v) })
                    }
                }
            }
        }
    }

    override suspend fun finalize() {
        job.cancelAndJoin()
    }

    override suspend fun sense(): NeighboursRssi = filterMap.mapValues { (_, v) -> v.mean().toInt() }
}

class SmartphoneSensorsContainer(private val neighboursDistance: Flow<NeighboursRssi>) : SensorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        this += DistanceSensor(neighboursDistance).apply { initialize() }
    }
}

suspend fun smartphoneSensorLogic(
    sensor: SensorsContainer,
    behaviourRef: BehaviourRef<NeighboursRssi>,
) {
    while (true) {
        sensor.get<DistanceSensor> {
            behaviourRef.sendToComponent(sense())
        }
        delay(250.milliseconds)
    }
}
