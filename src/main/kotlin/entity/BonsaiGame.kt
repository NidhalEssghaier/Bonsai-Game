package entity

import kotlinx.serialization.Serializable
import serializer.ArrayDequeGameStateSerializer

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
@Serializable
class BonsaiGame private constructor(
    var currentState: GameState,
    @Serializable(with = ArrayDequeGameStateSerializer::class)
    val undoStack: ArrayDeque<GameState>,
    @Serializable(with = ArrayDequeGameStateSerializer::class)
    val redoStack: ArrayDeque<GameState>,
)
{
    /**
     * Secondary public constructor for public access
     */
    constructor(
        gameSpeed: Int,
        players: List<Player>,
        goalCards: List<GoalCard>,
        drawStack: ArrayDeque<ZenCard>,
        openCards: MutableList<ZenCard>
    ) : this(
        GameState(gameSpeed, players, goalCards, drawStack, openCards),
        ArrayDeque(),
        ArrayDeque()
    )
}
