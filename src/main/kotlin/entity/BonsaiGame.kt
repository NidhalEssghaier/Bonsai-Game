package entity

import tools.aqua.bgw.util.Stack

/**
 * Entity to represent a game of "Bonsai"
 *
 * @property undoStack Saves previous actions.
 * @property redoStack Saves actions reverted by undo.
 * @param gameSpeed Value for the simulation speed
 * @param drawStack The card stack in the shop
 * @param openCards List of the available cards in the shop
 * @param players List of players in the configured sorting order
 * @param goalCards List of chosen goal cards
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
