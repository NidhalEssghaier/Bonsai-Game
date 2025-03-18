package entity

import helper.pop
import kotlinx.serialization.Serializable
import serializer.BonsaiGameSerializer

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
@Serializable(with = BonsaiGameSerializer::class)
class BonsaiGame private constructor(
    var currentState: GameState,
    val undoStack: ArrayDeque<GameState>,
    val redoStack: ArrayDeque<GameState>,
) {
    /**
     * Secondary public constructor for public access
     * @param gameSpeed Value for the simulation speed
     * @param players List of players in the configured sorting order
     * @param goalCards List of available goal cards
     * @param drawStack The card stack in the shop
     * @param openCards List of open cards in the shop
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

    /**
     * Constructor for the serializer
     * @param undoStack The undo stack
     * @param redoStack The redo stack
     */
    constructor(
        undoStack: ArrayDeque<GameState>,
        redoStack: ArrayDeque<GameState>,
    ) : this(
        undoStack.pop(),
        undoStack,
        redoStack,
    )
}
