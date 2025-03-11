package entity

import tools.aqua.bgw.util.Stack

/**
 * Entity to represent a game of "Bonsai"
 *
 * @property currentPlayer The active player, who can do actions.
 * @property endGameCounter Is used at the end of the game to allow every player only one last turn.
 * @property undoStack Saves previous actions.
 * @property redoStack Saves actions reverted by undo.
 * @property gameSpeed Value for the simulation speed
 * @property drawStack The card stack in the shop
 * @property openCards List of the available cards in the shop
 * @property players List of players in the configured sorting order
 * @property goalCards List of chosen goal cards
 */
class BonsaiGame(
    gameSpeed: Int,
    players: List<Player>,
    goalCards: List<GoalCard>,
    drawStack: Stack<ZenCard>,
    openCards: List<ZenCard>
)
{
    var currentState = GameState(gameSpeed, players, goalCards, drawStack, openCards)
    val undoStack: Stack<GameState> = Stack()
    val redoStack: Stack<GameState> = Stack()
}
