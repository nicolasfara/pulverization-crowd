package it.nicolasfarabegoli.pulverization.crowd.common

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.State
import it.nicolasfarabegoli.pulverization.crowd.smartphone.NeighboursDistances
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.coroutineScope
import org.koin.core.component.inject

sealed interface StateOps
object GetCurrentState : StateOps
data class CurrentState(val distance: NeighboursDistances) : StateOps

class StateComponent : State<StateOps> {
    override val context: Context by inject()

    override fun get(): StateOps = TODO()

    override fun update(newState: StateOps): StateOps {
        when (newState) {
            GetCurrentState -> {}
            is CurrentState -> {}
        }
        TODO()
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
