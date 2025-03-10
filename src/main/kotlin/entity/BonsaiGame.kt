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

    val gameSpeed: Int,
    val players: List<Player>,
    val goalCards: List<GoalCard>,
    val drawStack: Stack<ZenCard>,
    val openCards: List<ZenCard>
){
    var currentPlayer: Int = 0
    var endGameCounter: Int = 0
    val undoStack: Stack<BonsaiGame> = Stack()
    val redoStack: Stack<BonsaiGame> = Stack()




}
