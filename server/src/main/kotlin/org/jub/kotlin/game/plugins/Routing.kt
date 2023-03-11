package org.jub.kotlin.game.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jub.kotlin.game.configs.GamesConfig
import org.jub.kotlin.game.entities.Game

fun Application.configureRouting() {
    routing {
        get("/create-game") {
            val game = Game(call.parameters["name"] ?: error("Unexpected error"))
            val gameId = GamesConfig.lastGameId()
            GamesConfig.games[gameId] = game
            call.respondText(gameId.toString())
        }

        get("/games") {
            call.respondText(GamesConfig.gamesString())
        }
    }
}
