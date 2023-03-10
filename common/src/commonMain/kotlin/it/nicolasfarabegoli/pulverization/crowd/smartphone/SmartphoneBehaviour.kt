package it.nicolasfarabegoli.pulverization.crowd.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Behaviour
import it.nicolasfarabegoli.pulverization.core.BehaviourOutput
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
import kotlin.math.pow

class SmartphoneBehaviour : Behaviour<StateOps, CommunicationPayload, NeighboursRssi, Unit, Unit> {
    override val context: Context by inject()

    override fun invoke(
        state: StateOps,
        export: List<CommunicationPayload>,
        sensedValues: NeighboursRssi,
    ): BehaviourOutput<StateOps, CommunicationPayload, Unit, Unit> {
        val distances = sensedValues.mapValues { (_, rssi) ->
            10.0.pow((-64 - rssi) / (10 * 2.4))
        }
        return BehaviourOutput(state, CommunicationPayload(context.deviceID, distances), Unit, Unit)
    }
}

@Suppress("UNUSED_PARAMETER")
suspend fun smartphoneBehaviourLogic(
    behaviour: Behaviour<StateOps, CommunicationPayload, NeighboursRssi, Unit, Unit>,
    stateRef: StateRef<StateOps>,
    commRef: CommunicationRef<CommunicationPayload>,
    sensRef: SensorsRef<NeighboursRssi>,
    actRef: ActuatorsRef<Unit>,
) = coroutineScope {
    var neighboursComm = emptyList<CommunicationPayload>()
    val job = launch {
        commRef.receiveFromComponent().collect { newMessage ->
            if (newMessage.deviceId != "0") {
                neighboursComm = neighboursComm.filter { it.deviceId != newMessage.deviceId } + newMessage
            }
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
