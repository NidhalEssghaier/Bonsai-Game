package entity

import helper.copy
import tools.aqua.bgw.util.Stack

/**
 * Entity to represent the game state
 *
 * @property gameSpeed Value for the simulation speed
 * @property players List of players in the configured sorting order
 * @property goalCards List of chosen goal cards
 * @property drawStack The card stack in the shop
 * @property openCards List of the available cards in the shop
 * @property currentPlayer The active player, who can do actions.
 * @property endGameCounter Is used at the end of the game to allow every player only one last turn.
 */
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
    /**
     * Creates a deep copy of the game state.
     * @return A deep copy of the game state.
     */
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