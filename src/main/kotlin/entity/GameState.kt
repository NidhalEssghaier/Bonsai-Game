package entity

import helper.copy
import tools.aqua.bgw.util.Stack

class GameState(
    val gameSpeed: Int,
    val players: List<Player>,
    val goalCards: List<GoalCard>,
    val drawStack: Stack<ZenCard>,
    val openCards: List<ZenCard>,

    var currentPlayer: Int = 0,
    var endGameCounter: Int = 0
)
{
    fun copy(): GameState {
        return GameState(
            gameSpeed,
            players.map { it.copy() },
            goalCards,
            drawStack.copy(),
            openCards,
            currentPlayer = this.currentPlayer,
            endGameCounter = this.endGameCounter
        )
    }
}