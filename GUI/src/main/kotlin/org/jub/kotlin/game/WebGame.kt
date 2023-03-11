package org.jub.kotlin.game

import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.concurrent.thread

class WebGame(private val gameId: Int) : Game() {
    override var mySymbol: String = ""
    override var turnText = mutableStateOf("Waiting for player")
    override val isYourMove = mutableStateOf(false)
    private val lock = Any()
    private val connectionThread: Thread

    @Volatile
    private var command: Command? = null

    init {
        val client = HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = 20_000
            }
        }
        connectionThread = thread {
            try {
                runBlocking {
                    client.webSocket(
                        method = HttpMethod.Get,
                        host = "localhost",
                        port = 8080,
                        path = "/game/$gameId"
                    ) {
                        val receiveCommandRoutine = launch { receiveCommand() }
                        val sendCommandRoutine = launch { sendCommand() }
                        sendCommandRoutine.join()
                        receiveCommandRoutine.cancelAndJoin()
                    }
                }
                client.close()
            } catch (_: InterruptedException) {
                println("InterruptedException")
            }
        }
    }

    private suspend fun DefaultClientWebSocketSession.receiveCommand() {
        try {
            for (message in incoming) {
                message as? Frame.Text ?: continue
                val receivedText = message.readText()
                val command = Json.decodeFromString<Command>(receivedText)
                when (command.command) {
                    "start" -> start(command.info)
                    "moved" -> moved(command.info)
                    "update" -> update(command.info)
                    "finish" -> finish(command.info)
                    else -> error("Unexpected command")
                }
            }
        } catch (e: SerializationException) {
            println("Error while receiving: ${e.localizedMessage}")
        } catch (e: IllegalArgumentException) {
            println("Error while receiving: ${e.localizedMessage}")
        }
    }

    private fun start(state: Int) {
        if (state == 0) {
            mySymbol = "X"
            turnText.value = "Your turn, Your symbol is $mySymbol"
            isYourMove.value = true
        } else if (state == 1) {
            mySymbol = "O"
            turnText.value = "Enemy turn, Your symbol is $mySymbol"
        }
    }

    private fun moved(cell: Int) {
        updateCell(cell, mySymbol)
        isYourMove.value = false
        turnText.value = "Enemy turn, Your symbol is $mySymbol"
    }

    private fun update(cell: Int) {
        updateCell(cell, if (mySymbol == "X") "O" else "X")
        isYourMove.value = true
        needWarning.value = false
        turnText.value = "Your turn, Your symbol is $mySymbol"
    }

    private fun finish(state: Int) {
        needWarning.value = false
        isFinished.value = true
        isYourMove.value = false
        turnText.value = ""
        endGameMessage.value = when (state) {
            0 -> "You lost"
            1 -> "You won!"
            else -> "Draw"
        }
    }

    private suspend fun DefaultClientWebSocketSession.sendCommand() {
        while (true) {
            delay(100)
            command ?: continue
            try {
                send(Json.encodeToString(command ?: error("Unexpected error")))
            } catch (e: CancellationException) {
                println("Error while sending: ${e.javaClass}")
                return
            } finally {
                synchronized(lock) {
                    command = null
                }
            }
        }
    }

    override fun clean() {
        connectionThread.interrupt()
    }

    override fun makeMove(index: Int) {
        needWarning.value = false
        if (!isYourMove.value) {
            needWarning.value = true
            return
        }
        synchronized(lock) {
            command = Command("move", index)
        }
    }

    @Serializable
    private data class Command(val command: String, val info: Int = -1)
}
