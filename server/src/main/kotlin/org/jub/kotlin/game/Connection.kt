package org.jub.kotlin.game

import io.ktor.websocket.*
import java.util.concurrent.atomic.AtomicInteger

data class Connection(val session: DefaultWebSocketSession) {
    val id: Int = lastUserId.getAndIncrement()
    companion object {
        private val lastUserId = AtomicInteger(0)
    }
}
