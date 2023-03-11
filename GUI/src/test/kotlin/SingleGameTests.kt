import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import org.jub.kotlin.game.Game
import org.jub.kotlin.game.SingleGame
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class SingleGameTests {

    companion object {
        private val state1 = listOf(1, 1, 1, 0, 2, 0, 0, 2, 0).toMutableStates()
        private val state2 = listOf(1, 0, 0, 0, 0, 0, 0, 2, 0).toMutableStates()
        private val state3 = listOf(1, 2, 1, 1, 2, 1, 0, 2, 0).toMutableStates()
        private val state4 = listOf(2, 1, 1, 1, 2, 2, 2, 1, 1).toMutableStates()

        @JvmStatic
        private fun combinationsTestSources(): Stream<Arguments> = Stream.of(
            Arguments.of(state1, true, "You win, congratulations!!!"),
            Arguments.of(state2, false, ""),
            Arguments.of(state3, true, "You lost, try again"),
            Arguments.of(state4, true, "Draw")
        )

        @JvmStatic
        private fun makeMoveInputs(): Stream<Arguments> = List(20) { (0..8).random() }.map { Arguments.of(it) }.stream()

        private fun List<Int>.toMutableStates() =
            this.map { mutableStateOf(if (it == 1) "X" else if (it == 2) "O" else "") }
    }

    @ParameterizedTest
    @MethodSource("combinationsTestSources")
    fun getWinCombinationTest(
        state: List<MutableState<String>>,
        isFinishedExpected: Boolean,
        endMessageExpected: String
    ) {
        val singleGame = SingleGame()
        val contentField = Game::class.java.getDeclaredField("content")
        contentField.isAccessible = true
        contentField.set(singleGame, state)
        val checkOnFinishMethod = SingleGame::class.java.getDeclaredMethod("checkOnFinish")
        checkOnFinishMethod.isAccessible = true
        checkOnFinishMethod.invoke(singleGame)
        val isFinishedField = Game::class.java.getDeclaredField("isFinished")
        isFinishedField.isAccessible = true
        val isFinished = isFinishedField.get(singleGame) as MutableState<Boolean>
        Assertions.assertEquals(isFinishedExpected, isFinished.value)
        if (isFinished.value) {
            val endMessageField = Game::class.java.getDeclaredField("endGameMessage")
            endMessageField.isAccessible = true
            val endMessage = endMessageField.get(singleGame) as MutableState<String>
            Assertions.assertEquals(endMessageExpected, endMessage.value)
        }
    }

    @ParameterizedTest
    @MethodSource("makeMoveInputs")
    fun makeMoveTest(index: Int) {
        val singleGame = SingleGame()
        singleGame.makeMove(index)
        val contentField = Game::class.java.getDeclaredField("content")
        contentField.isAccessible = true
        val content = contentField.get(singleGame) as List<MutableState<String>>
        Assertions.assertEquals("X", content[index].value)
    }
}
