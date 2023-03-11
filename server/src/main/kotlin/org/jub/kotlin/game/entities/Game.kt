package org.jub.kotlin.game.entities

class Game(val name: String) {
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
    private val board = Array(9) { -1 }
    var turn = Turn.values().random()
    private val signs = (0..1).map { if (turn.index == it) 1 else 0 }
    val players = mutableListOf<Int>()
    var state = State.WAITING_FOR_PLAYER
        private set

    fun checkOnWin() = winCombinations.any {
        board[it[0]] != -1 && board[it[0]] == board[it[1]] && board[it[0]] == board[it[2]]
    }.also { if (it) state = State.FINISHED }

    fun checkOnDraw() = board.all { it != -1 }.also { if (it) state = State.FINISHED }

    fun start() {
        state = State.IN_PROGRESS
    }

    @Synchronized
    fun isMakedMove(playerId: Int, index: Int): Boolean {
        if (playerId != players[turn.index]) return false
        if (board[index] != -1) return false
        board[index] = signs[turn.index]
        turn = Turn.values().find { it != turn }!!
        return true
    }

    enum class Turn(val index: Int) {
        PLAYER1(0),
        PLAYER2(1),
        ;
    }

    enum class State {
        FINISHED, IN_PROGRESS, WAITING_FOR_PLAYER
    }
}
