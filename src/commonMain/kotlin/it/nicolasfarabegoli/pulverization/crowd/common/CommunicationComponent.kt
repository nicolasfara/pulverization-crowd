package it.nicolasfarabegoli.pulverization.crowd.common

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Communication
import it.nicolasfarabegoli.pulverization.crowd.smartphone.NeighboursDistances
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.core.component.inject

data class CommunicationPayload(val deviceId: String, val distances: NeighboursDistances)

class CommunicationComponent : Communication<CommunicationPayload> {
    override val context: Context by inject()
    override fun receive(): Flow<CommunicationPayload> {
        TODO("Not yet implemented")
    }

    override suspend fun send(payload: CommunicationPayload) {
        TODO("Not yet implemented")
    }
}

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
}
