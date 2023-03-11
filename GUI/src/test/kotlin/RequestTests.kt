import io.mockk.every
import io.mockk.mockkStatic
import org.jub.kotlin.game.getGameId
import org.jub.kotlin.game.getGames
import org.jub.kotlin.game.getRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RequestTests {

    @BeforeAll
    fun init() {
        mockkStatic("org.jub.kotlin.game.RequestsKt")
        every { getRequest("games") } returns "{\"1\":\"game1\", \"2\":\"game2\", \"3\":\"game3\"}"
        every { getRequest("create-game", mapOf("name" to "game4")) } returns "4"
        every { getRequest("create-game", mapOf("name" to "game5")) } returns "5"
    }

    @Test
    fun testGetGames() {
        Assertions.assertEquals(mapOf("1" to "game1", "2" to "game2", "3" to "game3"), getGames())
    }

    @Test
    fun getGameIdTest() {
        Assertions.assertEquals(4, getGameId("game4"))
        Assertions.assertEquals(5, getGameId("game5"))
    }

}
