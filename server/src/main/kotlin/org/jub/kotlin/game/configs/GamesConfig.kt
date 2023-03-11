package org.jub.kotlin.game.configs

import com.google.gson.Gson
import org.jub.kotlin.game.entities.Game
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

object GamesConfig {
    private val lastGameIdAtomic = AtomicInteger(0)
    val games = ConcurrentHashMap<Int, Game>()

    fun lastGameId() = lastGameIdAtomic.getAndIncrement()

    fun gamesString(): String =
        Gson().toJson(games.filterValues { it.state == Game.State.WAITING_FOR_PLAYER }
            .mapValues { it.value.name })
}
