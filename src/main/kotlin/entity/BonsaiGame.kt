package entity

/**
 * Entity to represent a game of "Bonsai"
 *
 * @param gameSpeed Value for the simulation speed
 * @param drawStack The card stack in the shop
 * @param openCards List of the available cards in the shop
 * @param players List of players in the configured sorting order
 * @param goalCards List of chosen goal cards
 * @property undoStack Saves previous game state.
 * @property redoStack Saves game states reverted by undo.
 * @property currentState The current game state.
 */
class BonsaiGame(
    gameSpeed: Int,
    players: List<Player>,
    goalCards: List<GoalCard>,
    drawStack: ArrayDeque<ZenCard>,
    openCards: MutableList<ZenCard>
)
{
    var currentState = GameState(gameSpeed, players, goalCards, drawStack, openCards)
    val undoStack: ArrayDeque<GameState> = ArrayDeque()
    val redoStack: ArrayDeque<GameState> = ArrayDeque()
}
