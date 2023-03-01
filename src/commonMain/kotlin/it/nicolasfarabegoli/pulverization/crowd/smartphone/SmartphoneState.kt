package it.nicolasfarabegoli.pulverization.crowd.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.State
import org.koin.core.component.inject

sealed interface StateOps
object GetCurrentState : StateOps
data class CurrentState(val distance: NeighboursDistances) : StateOps

class SmartphoneState : State<StateOps> {
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
