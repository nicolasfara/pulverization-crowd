package it.nicolasfarabegoli.pulverization.crowd.common

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Communication
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.inject

actual class CommunicationComponent : Communication<CommunicationPayload> {
    override val context: Context by inject()
    override fun receive(): Flow<CommunicationPayload> {
        TODO("Not yet implemented")
    }

    override suspend fun send(payload: CommunicationPayload) {
        TODO("Not yet implemented")
    }
}
