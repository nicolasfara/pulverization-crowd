package it.nicolasfarabegoli.pulverization.crowd

import it.nicolasfarabegoli.pulverization.core.StateComponent
import it.nicolasfarabegoli.pulverization.core.BehaviourComponent
import it.nicolasfarabegoli.pulverization.core.ActuatorsComponent
import it.nicolasfarabegoli.pulverization.core.CommunicationComponent
import it.nicolasfarabegoli.pulverization.core.SensorsComponent
import it.nicolasfarabegoli.pulverization.dsl.Cloud
import it.nicolasfarabegoli.pulverization.dsl.Device
import it.nicolasfarabegoli.pulverization.dsl.pulverizationConfig

val config = pulverizationConfig {
    logicalDevice("estimator") {
        StateComponent and BehaviourComponent and ActuatorsComponent and CommunicationComponent deployableOn Device
    }
    logicalDevice("smartphone") {
        StateComponent and BehaviourComponent and SensorsComponent and CommunicationComponent deployableOn Device
    }
    logicalDevice("smartphone-offloaded") {
        StateComponent and BehaviourComponent deployableOn Cloud
        CommunicationComponent and SensorsComponent deployableOn Device
    }
}
