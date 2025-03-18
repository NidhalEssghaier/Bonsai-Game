package entity

import kotlinx.serialization.Serializable
import serializer.ArrayDequeGameStateSerializer

/**
 * Entity to represent a game of "Bonsai"
 *
 * @constructor gameSpeed Value for the simulation speed
 * @constructor drawStack The card stack in the shop
 * @constructor openCards List of the available cards in the shop
 * @constructor players List of players in the configured sorting order
 * @constructor goalCards List of chosen goal cards
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
) {
    /**
     * Secondary public constructor for public access
     */
    constructor(
        gameSpeed: Int,
        players: List<Player>,
        goalCards: MutableList<GoalCard?>,
        drawStack: ArrayDeque<ZenCard>,
        openCards: MutableList<ZenCard>,
    ) : this(
        GameState(gameSpeed, players, goalCards, drawStack, openCards),
        ArrayDeque(),
        ArrayDeque(),
    )
}
