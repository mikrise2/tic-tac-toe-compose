package org.jub.kotlin.game

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

const val BOARD_SIZE: Int = 9
abstract class Game {
    protected val content = List(BOARD_SIZE) { mutableStateOf("") }
    abstract val isYourMove: MutableState<Boolean>
    abstract var mySymbol: String
    abstract var turnText: MutableState<String>
    val needWarning = mutableStateOf(false)
    val isFinished = mutableStateOf(false)
    val endGameMessage = mutableStateOf("")

    abstract fun clean()
    abstract fun makeMove(index: Int)

    protected fun updateCell(index: Int, symbol: String) {
        content[index].value = symbol
    }

    fun getCellContent(index: Int) = content[index]
}
