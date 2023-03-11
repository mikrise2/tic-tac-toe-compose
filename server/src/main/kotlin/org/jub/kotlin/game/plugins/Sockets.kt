package org.jub.kotlin.game.plugins

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jub.kotlin.game.Connection
import org.jub.kotlin.game.configs.GamesConfig
import java.util.*
import kotlin.collections.LinkedHashSet

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
        webSocket("/game/{id}") {
            val id = call.parameters["id"]?.toInt()
            val game = GamesConfig.games[id]
            val thisConnection = Connection(this)
            game?.players?.add(thisConnection.id)
            connections += thisConnection
            try {
                if (game?.players?.size == 2) {
                    val players = game.players.map { playerId -> connections.find { it.id == playerId } }
                    GamesConfig.games.remove(id!!)
                    game.start()
                    players.find { it?.id == players[game.turn.index]?.id }?.session?.send(startCommandToString(0))
                    players.find { it?.id != players[game.turn.index]?.id }?.session?.send(startCommandToString(1))
                }
                for (frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    val command = Json.decodeFromString<Command>(receivedText)
                    val enemy = game!!.players.map { playerId -> connections.find { it.id == playerId } }
                        .find { it != thisConnection }
                    when (command.command) {
                        "move" -> {
                            if (game.isMakedMove(thisConnection.id, command.info)) {
                                enemy!!.session.send(Json.encodeToString(Command("update", command.info)))
                                thisConnection.session.send(Json.encodeToString(Command("moved", command.info)))
                                val win = game.checkOnWin()
                                val draw = game.checkOnDraw()
                                if (win) {
                                    enemy.session.send(Json.encodeToString(Command("finish", 0)))
                                    thisConnection.session.send(Json.encodeToString(Command("finish", 1)))
                                } else if (draw) {
                                    enemy.session.send(Json.encodeToString(Command("finish", 2)))
                                    thisConnection.session.send(Json.encodeToString(Command("finish", 2)))
                                }
                            }
                        }
                        else -> println("Unknown command")
                    }
                }
            } catch (e: CancellationException) {
                println(e.localizedMessage)
            } finally {
                connections -= thisConnection
                GamesConfig.games.remove(id)
            }
        }
    }
}

private fun startCommandToString(state: Int) = Json.encodeToString(Command("start", state))

@Serializable
data class Command(val command: String, val info: Int = -1)
