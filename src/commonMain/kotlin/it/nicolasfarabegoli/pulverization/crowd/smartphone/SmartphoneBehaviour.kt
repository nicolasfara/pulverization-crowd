package it.nicolasfarabegoli.pulverization.crowd.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.*
import it.nicolasfarabegoli.pulverization.crowd.common.CommunicationPayload
import it.nicolasfarabegoli.pulverization.crowd.common.GetCurrentState
import it.nicolasfarabegoli.pulverization.crowd.common.StateOps
import it.nicolasfarabegoli.pulverization.runtime.componentsref.ActuatorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.CommunicationRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.SensorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.StateRef
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.inject

class SmartphoneBehaviour : Behaviour<StateOps, CommunicationPayload, NeighboursDistances, Unit, Unit> {
    override val context: Context by inject()

    override fun invoke(
        state: StateOps,
        export: List<CommunicationPayload>,
        sensedValues: NeighboursDistances
    ): BehaviourOutput<StateOps, CommunicationPayload, Unit, Unit> {
        TODO("Not yet implemented")
    }
}

suspend fun smartphoneBehaviourLogic(
    behaviour: Behaviour<StateOps, CommunicationPayload, NeighboursDistances, Unit, Unit>,
    stateRef: StateRef<StateOps>,
    commRef: CommunicationRef<CommunicationPayload>,
    sensRef: SensorsRef<NeighboursDistances>,
    actRef: ActuatorsRef<Unit>,
) = coroutineScope {
    var neighboursComm = emptyList<CommunicationPayload>()
    val job = launch {
        commRef.receiveFromComponent().collect { newMessage ->
            neighboursComm = neighboursComm.filter { it.deviceId != newMessage.deviceId } + newMessage
        }
    }
    sensRef.receiveFromComponent().collect { sensors ->
        stateRef.sendToComponent(GetCurrentState)
        val state = stateRef.receiveFromComponent().first()
        val (newState, newComm, _, _) = behaviour(state, neighboursComm, sensors)
        stateRef.sendToComponent(newState)
        commRef.sendToComponent(newComm)
    }
    job.join()
}