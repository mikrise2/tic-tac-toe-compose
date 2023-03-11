import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.jub.kotlin.game.configs.GamesConfig
import org.jub.kotlin.game.plugins.configureRouting
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opentest4j.TestAbortedException

class ApplicationTest {

    @Test
    fun testRouting() = testApplication {
        application {
            configureRouting()
        }
        for (i in 0..10) {
            val response = client.get("/create-game") {
                parameter("name", "new game$i")
            }
            Assertions.assertEquals(HttpStatusCode.OK, response.status)
            Assertions.assertEquals("new game$i", GamesConfig.games[i]?.name ?: throw TestAbortedException())
            Assertions.assertEquals(i.toString(), response.bodyAsText())
        }
        val response = client.get("/games")
        val games = Json.decodeFromString<Map<String, String>>(response.bodyAsText())
        Assertions.assertEquals(11, games.size)
        Assertions.assertEquals((0..10).map { it.toString() }.toSet(), games.keys)
        Assertions.assertEquals((0..10).map { "new game$it" }.toSet(), games.values.toSet())
    }

}
