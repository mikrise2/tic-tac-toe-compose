package org.jub.kotlin.game

import androidx.compose.runtime.mutableStateOf

class SingleGame : Game() {
    override var mySymbol: String = "X"
    override var turnText = mutableStateOf("Your symbol is X")
    override val isYourMove = mutableStateOf(true)
    private val winCombinations =
        listOf(
            listOf(0, 3, 6),
            listOf(1, 4, 7),
            listOf(2, 5, 8),
            listOf(0, 1, 2),
            listOf(3, 4, 5),
            listOf(6, 7, 8),
            listOf(0, 4, 8),
            listOf(2, 4, 6)
        )

    override fun makeMove(index: Int) {
        if (!isFinished.value) {
            if (content[index].value != "") {
                needWarning.value = true
                return
            }
            updateCell(index, mySymbol)
            checkOnFinish()
            isYourMove.value = false
            computerMove()
        }
    }

    private fun computerMove() {
        if (isFinished.value)
            return
        val freeCellIndex = content.indices.filter { content[it].value == "" }.random()
        updateCell(freeCellIndex, "O")
        isYourMove.value = true
        checkOnFinish()
    }

    private fun getWinCombination() = winCombinations.firstOrNull {
        content[it[0]].value != "" && content[it[0]].value == content[it[1]].value &&
                content[it[0]].value == content[it[2]].value
    }

    private fun checkOnFinish() {
        val winCombination = getWinCombination()
        winCombination?.let {
            finish(content[winCombination[0]].value)
            return
        }
        if (content.all { it.value != "" }) {
            isFinished.value = true
            isYourMove.value = false
            endGameMessage.value = "Draw"
        }
    }

    private fun finish(winSymbol: String) {
        isFinished.value = true
        endGameMessage.value = if (mySymbol == winSymbol) {
            "You win, congratulations!!!"
        } else {
            "You lost, try again"
        }
    }

    override fun clean() = Unit
}
