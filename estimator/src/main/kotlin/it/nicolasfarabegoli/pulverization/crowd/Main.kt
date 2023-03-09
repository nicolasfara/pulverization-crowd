package it.nicolasfarabegoli.pulverization.crowd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import it.nicolasfarabegoli.pulverization.crowd.common.CommunicationComponent
import it.nicolasfarabegoli.pulverization.crowd.common.StateComponent
import it.nicolasfarabegoli.pulverization.crowd.common.communicationComponentLogic
import it.nicolasfarabegoli.pulverization.crowd.common.stateComponentLogic
import it.nicolasfarabegoli.pulverization.crowd.estimator.EstimatorActuatorsContainer
import it.nicolasfarabegoli.pulverization.crowd.estimator.EstimatorBehaviour
import it.nicolasfarabegoli.pulverization.crowd.estimator.RGB
import it.nicolasfarabegoli.pulverization.crowd.estimator.estimatorActuatorLogic
import it.nicolasfarabegoli.pulverization.crowd.estimator.estimatorBehaviourLogic
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.actuatorsLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.behaviourLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.communicationLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.stateLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

/**
 * TODO.
 */
fun main() = application {
    var color by remember { mutableStateOf(Color.Red) }
    val rgbFlow = MutableSharedFlow<RGB>()
    val coroutineScope = rememberCoroutineScope()

    coroutineScope.launch {
        val platform = pulverizationPlatform(config.getDeviceConfiguration("estimator")!!) {
            behaviourLogic(EstimatorBehaviour(), ::estimatorBehaviourLogic)
            stateLogic(StateComponent(), ::stateComponentLogic)
            communicationLogic(CommunicationComponent("localhost"), ::communicationComponentLogic)
            actuatorsLogic(EstimatorActuatorsContainer(rgbFlow), ::estimatorActuatorLogic)

            withPlatform { RabbitmqCommunicator("localhost") }
            withRemotePlace { defaultRabbitMQRemotePlace() }
            withContext { deviceID("0") }
        }
        platform.start().joinAll()
        platform.stop()
    }

    coroutineScope.launch {
        rgbFlow.collect {
            color = Color(it.red, it.green, it.blue)
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Estimator pulverization",
        state = rememberWindowState(width = 300.dp, height = 300.dp),
    ) {
        MaterialTheme {
            Column(Modifier.background(color).fillMaxSize(), Arrangement.spacedBy(5.dp)) {
                Text("Dio")
            }
        }
    }
}
