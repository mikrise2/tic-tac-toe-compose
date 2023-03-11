package org.jub.kotlin.game

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlin.system.exitProcess

typealias GamesState = MutableState<Map<String, String>>

private enum class ProgramState {
    CHOOSING_GAME, MAIN_MENU, MULTI_PLAYER_GAME, SINGLE_PLAYER_GAME
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "TicTacToe",
        state = rememberWindowState(width = 800.dp, height = 800.dp)
    ) {
        val gameId = remember { mutableStateOf(0) }
        val state = remember { mutableStateOf(ProgramState.MAIN_MENU) }
        when (state.value) {
            ProgramState.MAIN_MENU -> startWindow(state)
            ProgramState.CHOOSING_GAME -> chooseGame(state, gameId)
            ProgramState.MULTI_PLAYER_GAME -> playGame(WebGame(gameId.value), state)
            else -> playGame(SingleGame(), state)
        }
    }
}

@Composable
private fun startWindow(state: MutableState<ProgramState>) {
    MaterialTheme {
        backButton { exitProcess(0) }
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center.also { Arrangement.spacedBy(10.dp) }) {
            menuButton("Play with computer") { state.value = ProgramState.SINGLE_PLAYER_GAME }
            menuButton("Multiplayer game") { state.value = ProgramState.CHOOSING_GAME }
            menuButton("Exit") { exitProcess(0) }
        }
    }
}

@Composable
private fun chooseGame(
    state: MutableState<ProgramState>,
    gameId: MutableState<Int>
) {
    val games = remember { mutableStateOf(getGames()) }
    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(10.dp)
        ) {
            val stateVertical = rememberScrollState(0)
            chooseElements(games, stateVertical, gameId, state)
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(stateVertical)
            )
        }
        backButton { state.value = ProgramState.MAIN_MENU }
        refreshButton(games)
    }
}

@Composable
private fun chooseElements(
    games: GamesState,
    stateVertical: ScrollState,
    gameId: MutableState<Int>,
    state: MutableState<ProgramState>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(stateVertical)
            .padding(end = 12.dp, bottom = 12.dp)
    ) {
        Column(Modifier.align(Alignment.Center).width(400.dp)) {
            createForm(gameId, state)
            games.value.entries.forEach { game ->
                selectGameButton(game, gameId, state)
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
    }
}

@Composable
private fun createForm(gameId: MutableState<Int>, state: MutableState<ProgramState>) {
    var text by remember { mutableStateOf("") }
    Row {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.height(50.dp)
        )
        Button(modifier = Modifier.padding(start = 15.dp).fillMaxWidth(), onClick = {
            gameId.value = getGameId(text)
            state.value = ProgramState.MULTI_PLAYER_GAME
        }) {
            Text("create")
        }
    }
}

@Composable
private fun ColumnScope.selectGameButton(
    game: Map.Entry<String, String>,
    gameId: MutableState<Int>,
    state: MutableState<ProgramState>
) {
    Button(modifier = Modifier.width(400.dp).align(Alignment.CenterHorizontally), onClick = {
        gameId.value = game.key.toInt()
        state.value = ProgramState.MULTI_PLAYER_GAME
    }) {
        Text(game.value)
    }
}

@Composable
private fun ColumnScope.menuButton(text: String, lambda: Runnable = Runnable { }) {
    Button(modifier = Modifier.width(300.dp).align(Alignment.CenterHorizontally),
        onClick = {
            lambda.run()
        }) {
        Text(text)
    }
}

@Composable
private fun playGame(game: Game, state: MutableState<ProgramState>) {
    val isFinished = remember { game.isFinished }
    val endGameMessage = remember { game.endGameMessage }
    val turnText = remember { game.turnText }
    val needWarning = remember { game.needWarning }
    Box(modifier = Modifier.fillMaxSize()) {
        backButton {
            state.value = ProgramState.MAIN_MENU
            game.clean()
        }
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.Center),
            verticalArrangement = Arrangement.Center
        ) {
            textByCondition(mutableStateOf(true), turnText)
            gameBoard(game, isFinished)
            textByCondition(needWarning, "it isn't your turn")
            textByCondition(isFinished, endGameMessage)
        }
    }
}

@Composable
private fun ColumnScope.gameBoard(
    game: Game, isFinished: MutableState<Boolean>
) {
    for (i in 0..2) {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            for (y in 0..2) {
                val index = i * 3 + y
                cell(game, isFinished, index)
            }
        }
    }
}

@Composable
private fun RowScope.cell(
    game: Game, isFinished: MutableState<Boolean>,
    index: Int
) {
    Button(modifier = Modifier
        .width(100.dp)
        .height(100.dp)
        .align(CenterVertically)
        .padding(3.dp),
        onClick = {
            if (!isFinished.value) {
                game.makeMove(index)
            }
        }) {
        val textValue = remember { game.getCellContent(index) }
        Text(textValue.value)
    }
}

@Composable
private fun ColumnScope.textByCondition(condition: MutableState<Boolean>, text: MutableState<String>) {
    textByCondition(condition, text.value)
}

@Composable
private fun ColumnScope.textByCondition(condition: MutableState<Boolean>, text: String) {
    if (condition.value) {
        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(text)
        }
    }
}

@Composable
private fun backButton(function: Runnable) {
    Button(modifier = Modifier.padding(4.dp),
        onClick = {
            function.run()
        }) {
        Text("Back")
    }
}

@Composable
private fun BoxScope.refreshButton(games: GamesState) {
    Button(modifier = Modifier.align(Alignment.TopEnd), onClick = {
        games.value = getGames()
    }) {
        Text("Refresh")
    }
}

