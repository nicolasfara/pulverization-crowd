package it.nicolasfarabegoli.pulverization.crowd.estimator

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
import org.koin.core.component.inject

class EstimatorBehaviour : Behaviour<StateOps, CommunicationPayload, Unit, RGB, Unit> {
    override val context: Context by inject()

    override fun invoke(
        state: StateOps,
        export: List<CommunicationPayload>,
        sensedValues: Unit
    ): BehaviourOutput<StateOps, CommunicationPayload, RGB, Unit> {
        TODO("Not yet implemented")
    }
}

@Suppress("UNUSED_PARAMETER")
suspend fun estimatorBehaviourLogic(
    behaviour: Behaviour<StateOps, CommunicationPayload, Unit, RGB, Unit>,
    stateRef: StateRef<StateOps>,
    commRef: CommunicationRef<CommunicationPayload>,
    sensRef: SensorsRef<Unit>,
    actRef: ActuatorsRef<RGB>,
) = coroutineScope {
    var neighboursMessages = emptyList<CommunicationPayload>()
    commRef.receiveFromComponent().collect { message ->
        neighboursMessages = neighboursMessages.filter { it.deviceId != message.deviceId } + message
        stateRef.sendToComponent(GetCurrentState)
        val state = stateRef.receiveFromComponent().first()
        val (newState, newComm, action, _) = behaviour(state, neighboursMessages, Unit)
        stateRef.sendToComponent(newState)
        commRef.sendToComponent(newComm)
        actRef.sendToComponent(action)
    }
}
