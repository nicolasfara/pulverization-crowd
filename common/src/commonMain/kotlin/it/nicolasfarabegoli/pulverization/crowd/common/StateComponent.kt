package it.nicolasfarabegoli.pulverization.crowd.common

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.State
import it.nicolasfarabegoli.pulverization.crowd.smartphone.NeighboursRssi
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
sealed interface StateOps

@Serializable
object GetCurrentState : StateOps

@Serializable
data class CurrentState(val distance: NeighboursRssi) : StateOps

class StateComponent : State<StateOps> {
    override val context: Context by inject()

    private var currentState = CurrentState(emptyMap())

    override fun get(): StateOps = currentState

    override fun update(newState: StateOps): StateOps {
        return when (newState) {
            GetCurrentState -> currentState
            is CurrentState -> {
                val oldState = currentState
                currentState = newState
                oldState
            }
        }
    }
}

suspend fun stateComponentLogic(
    state: State<StateOps>,
    behaviourRef: BehaviourRef<StateOps>,
) = coroutineScope {
    behaviourRef.receiveFromComponent().collect {
        when (it) {
            is CurrentState -> state.update(it)
            GetCurrentState -> behaviourRef.sendToComponent(state.get())
        }
    }
}
