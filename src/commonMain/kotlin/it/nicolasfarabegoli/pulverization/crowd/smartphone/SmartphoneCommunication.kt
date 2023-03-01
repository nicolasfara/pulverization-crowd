package it.nicolasfarabegoli.pulverization.crowd.smartphone

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Communication
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject

data class CommunicationPayload(val deviceId: String, val distances: NeighboursDistances)

class SmartphoneCommunication : Communication<CommunicationPayload> {
    override val context: Context by inject()
    override fun receive(): Flow<CommunicationPayload> {
        TODO("Not yet implemented")
    }

    override suspend fun send(payload: CommunicationPayload) {
        TODO("Not yet implemented")
    }
}
