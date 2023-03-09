package it.nicolasfarabegoli.pulverization.crowd.common

import it.nicolasfarabegoli.pulverization.core.Communication
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

typealias NeighboursDistances = Map<String, Double>

@Serializable
data class CommunicationPayload(val deviceId: String, val distances: NeighboursDistances)

expect class CommunicationComponent : Communication<CommunicationPayload>

suspend fun communicationComponentLogic(
    comm: Communication<CommunicationPayload>,
    behaviourRef: BehaviourRef<CommunicationPayload>,
) = coroutineScope {
    val j1 = launch {
        comm.receive().collect {
            behaviourRef.sendToComponent(it)
        }
    }
    val j2 = launch {
        behaviourRef.receiveFromComponent().collect {
            comm.send(it)
        }
    }
    setOf(j1, j2).joinAll()
}
