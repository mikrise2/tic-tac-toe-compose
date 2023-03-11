package org.jub.kotlin.game

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun getGameId(text: String): Int =
    Json.decodeFromString(getRequest("create-game", mapOf("name" to text)))

fun getGames(): Map<String, String> = Json.decodeFromString(getRequest("games"))

fun getRequest(path: String, parameters: Map<String, String> = emptyMap()): String {
    val client = HttpClient(CIO)
    val answer: String
    runBlocking {
        answer = client.get("http://localhost:8080/$path") {
            url {
                parameters.forEach { parameter(it.key, it.value) }
            }
        }.body()
    }
    return answer
}
